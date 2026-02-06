(ns api
  (:require
    ["fast-xml-parser" :refer [XMLParser]]
    [promise :refer [js-promise? wait]]
    [interop :refer [js-error parse-int]]
    [http :as h]
    [marshall :refer [marshall]]))

(def ^:private base-url "https://boardgamegeek.com")

(defn parse-xml [xml]
  {:pre  [(string? xml)]
   :post [(map? %)]}
  (-> (XMLParser. #js{:ignoreAttributes    false
                      :ignoreDeclaration   true
                      :attributeNamePrefix "$_"})
      (.parse xml)
      js->clj))

(defn- ->collection-map [m game]
  (let [id  (parse-int (get game "$_objectid"))
        own (= "1" (get-in game ["status" "$_own"]))]
    (if (get m id)
      m
      (assoc m id own))))

(defn- ->collection-row [username [id own]]
  {:id id
   :own own
   :username username})

(defn fetch-collection [api-key username]
  (-> (h/fetch-with-backoff
       (str base-url
            "/xmlapi2/collection?"
            (h/map->params {:brief 1 :username username}))
       {:headers
        {:Authorization
         (str "Bearer " api-key)}})
      (.then (fn [response]
               (case response.status
                     200 (.text response)
                     202 (-> (wait 5000)
                             (.then #(fetch-collection api-key username)))
                     (throw (js-error "Could not pull collection" response)))))))

(defn get-collection [api-key username]
  (-> (fetch-collection api-key username)
      (.then (fn [xml]
               (let [tree  (parse-xml xml)
                     games (get-in tree ["items" "item"])]
                 (if-not games
                   (throw (js-error "Invalid username" username))
                   (->> games
                        (reduce ->collection-map {})
                        (map (partial ->collection-row username)))))))))

(defn get-games [api-key ids]
  {:pre [(string? api-key)
         (sequential? ids)]
   :post [(js-promise? %)]}
  (-> (h/fetch-with-backoff (str base-url
                                 "/xmlapi2/thing?"
                                 (h/map->params {:stats 1
                                                 :type [:boardgame
                                                        :boardgameexpansion]
                                                 :id ids}))
                            {:headers {:Authorization (str "Bearer " api-key)}})
      (.then (fn [response]
               (if response.ok
                 (.text response)
                 (throw (js-error "Could not retrieve games" response)))))
      (.then #(as-> % <>
                    (parse-xml <>)
                    (get-in <> ["items" "item"])
                    (if (map? <>)
                      [(marshall <>)]
                      (map marshall <>))))))

(defn get-plays [api-key game-id page]
  {:pre [(pos-int? game-id)
         (pos-int? page)]
   :post [(js-promise? %)]}
  (-> (h/fetch-with-backoff (str base-url
                                 "/xmlapi2/plays?"
                                 (h/map->params {:type "thing"
                                                 :subtype "boardgame"
                                                 :id game-id
                                                 :page page}))
                            {:headers {:Authorization (str "Bearer " api-key)}})
      (.then (fn [response]
               (if response.ok
                 (.text response)
                 (throw (js-error "Could not retrieve plays" response)))))
      (.then #(as-> % <>
                    (parse-xml <>)
                    (get-in <> ["plays" "play"] [])
                    (map (fn [{:strs [$_id $_length players]}]
                           [(js/parseInt $_id 10)
                            game-id
                            (js/parseInt $_length 10)
                            (some-> players (get "player") count)])
                         <>)))))

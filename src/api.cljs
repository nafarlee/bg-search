(ns api
  (:require
    ["fast-xml-parser" :refer [XMLParser]]
    ["url" :refer [URL URLSearchParams]]
    [promise :refer [js-promise? wait]]
    [interop :refer [js-error parse-int]]
    [http :as h]
    [marshall :refer [marshall]]))

(def ^:private base-url "https://boardgamegeek.com")

(defn- construct-url [base path qp]
  {:pre [(string? base)
         (string? path)
         (map? qp)]
   :post [(string? %)]}
  (let [u (URL. path base)]
    (set! (.-search u) (URLSearchParams. (clj->js qp)))
    (.toString u)))

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

(defn get-collection [username]
  (-> (construct-url base-url "xmlapi2/collection" {:brief 1 :username username})
      h/get
      (.then
       (fn [res]
         (case (:status res)
               202 (.then (wait 5000) #(get-collection username))
               200 (let [xml   (:body res)
                         tree  (parse-xml xml)
                         games (get-in tree ["items" "item"])]
                     (if games
                       (->> games
                            (reduce ->collection-map {})
                            (map (partial ->collection-row username)))
                       (throw (js-error "Invalid username" username))))
               (throw (js-error "Could not pull collection" res)))))))

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
                 (throw (ex-info "Could not retrieve games"
                                 {:response response})))))
      (.then #(as-> % <>
                    (parse-xml <>)
                    (get-in <> ["items" "item"])
                    (if (map? <>)
                      [(marshall <>)]
                      (map marshall <>))))))

(defn get-plays [game-id page]
  {:pre [(pos-int? game-id)
         (pos-int? page)]
   :post [(js-promise? %)]}
  (-> (h/fetch-with-backoff (str base-url
                                 "/xmlapi2/plays?"
                                 (h/map->params {:type "thing"
                                                 :subtype "boardgame"
                                                 :id game-id
                                                 :page page}))
                            {:headers
                             {:Authorization
                              (str "Bearer " js/process.env.BGG_API_KEY)}})
      (.then (fn [response]
               (if response.ok
                 (.text response)
                 (throw (ex-info "Could not retrieve plays"
                                 {:response response})))))
      (.then #(as-> % <>
                    (parse-xml <>)
                    (get-in <> ["plays" "play"] [])
                    (map (fn [{:strs [$_id $_length players]}]
                           [(js/parseInt $_id 10)
                            game-id
                            (js/parseInt $_length 10)
                            (some-> players (get "player") count)])
                         <>)))))

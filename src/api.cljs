(ns api
  (:require
    ["html-entities" :as he]
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
  (js->clj
   (.parse
    (XMLParser. #js{:ignoreAttributes    false
                    :ignoreDeclaration   true
                    :attributeNamePrefix "$_"})
    xml)))

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

(defn get-games [ids]
  {:pre [(sequential? ids)]
   :post [(js-promise? %)]}
  (-> (construct-url base-url "xmlapi2/thing" {:stats 1
                                               :type ["boardgame" "boardgameexpansion"]
                                               :id ids})
      h/get
      (.then #(as-> % $
                    (h/unwrap $)
                    (parse-xml $)
                    (get-in $ ["items" "item"])
                    (if (map? $)
                      [(marshall $)]
                      (map marshall $))))))

(defn get-plays [game-id page]
  {:pre [(pos-int? game-id)
         (pos-int? page)]
   :post [(js-promise? %)]}
  (let [url         (construct-url base-url
                                   "xmlapi2/plays"
                                   {:type "thing"
                                    :subtype "boardgame"
                                    :id game-id
                                    :page page})
        play->chunk (fn [{:strs [$_id $_length players]}]
                      [(js/parseInt $_id 10)
                       game-id
                       (js/parseInt $_length 10)
                       (some-> players (get "player") count)])]
    (.then (h/get url)
           #(as-> % $
                  (h/unwrap $)
                  (parse-xml $)
                  (get-in $ ["plays" "play"] [])
                  (map play->chunk $)))))

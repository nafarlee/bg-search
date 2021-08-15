(ns api
  (:require
    ["fast-xml-parser" :as fxp]
    ["url" :refer [URL URLSearchParams]]
    [promise :refer [js-promise? wait]]
    [clojure.string :refer [join]]
    [http :as h]
    [marshall :refer [marshall]]))

(def ^:private base-url "https://www.boardgamegeek.com")

(defn- construct-url [base path qp]
  {:pre [(string? base)
         (string? path)
         (map? qp)]
   :post [(string? %)]}
  (let [u (URL. path base)]
    (set! (.-search u) (URLSearchParams. (clj->js qp)))
    (.toString u)))

(defn- parse-xml [xml]
  {:pre [(string? xml)]
   :post [(map? %)]}
  (js->clj (.parse fxp xml #js{:ignoreAttributes false :attributeNamePrefix "$_"})))

(defn get-collection [username]
  (-> (construct-url base-url "xmlapi2/collection" {:brief 1 :username username})
      h/get
      (.then (fn [res]
               (case (:status res)
                     202 (.then (wait 5000) #(get-collection username))
                     200 (as-> res $
                               (:body $)
                               (parse-xml $)
                               (get-in $ ["items" "item"])
                               (map #(hash-map :id  (js/parseInt (get % "$_objectid") 10)
                                               :username username
                                               :own (== "1" (get-in % ["status" "$_own"])))
                                    $))
                     (throw res))))))

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

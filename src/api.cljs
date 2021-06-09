(ns api
  (:require
    ["fast-xml-parser" :as fxp]
    ["url" :refer [URL URLSearchParams]]
    [promise :refer [js-promise?]]
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

(defn get-games [ids]
  {:pre [(sequential? ids)]
   :post [(js-promise? %)]}
  (-> (construct-url base-url "xmlapi2/thing" {:stats 1
                                               :type ["boardgame" "boardgameexpansion"]
                                               :id ids})
      h/get
      (.then #(as-> % $
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
                  (parse-xml $)
                  (get-in $ ["plays" "play"] [])
                  (map play->chunk $)))))

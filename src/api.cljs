(ns api
  (:require
    ["fast-xml-parser" :as fxp]
    ["url" :refer [URL URLSearchParams]]
    [clojure.string :refer [join]]
    [http :as h]
    [marshall :refer [marshall]]))

(def ^:private base-url "https://www.boardgamegeek.com")

(defn- construct-url [base path qp]
  (let [u (URL. path base)]
    (set! (.-search u) (URLSearchParams. (clj->js qp)))
    (.toString u)))

(defn- parse-xml [xml]
  (js->clj (.parse fxp xml #js{:ignoreAttributes false :attributeNamePrefix "$_"})))

(defn get-games [ids]
  (-> (construct-url base-url "xmlapi2/thing" {:stats 1
                                               :type ["boardgame" "boardgameexpansion"]
                                               :id ids})
      h/get
      (.then #(as-> % $
                    (parse-xml $)
                    (get-in $ ["items" "item"])
                    (if (map? $)
                      [(marshall $)]
                      (map marshall $))
                    (clj->js $)))))

(defn get-plays [game-id page]
  (-> (construct-url base-url "xmlapi2/plays" {:type "thing"
                                               :subtype "boardgame"
                                               :id game-id
                                               :page page})
      h/get
      (.then (fn [xml]
               (as-> xml <>
                     (parse-xml <>)
                     (get-in <> ["plays" "play"] [])
                     (map #(vector
                            (js/parseInt (get % "$_id") 10)
                            game-id
                            (js/parseInt (get % "$_length") 10)
                            (some-> % (get-in ["players" "player"]) count))
                          <>)
                     (clj->js <>))))))

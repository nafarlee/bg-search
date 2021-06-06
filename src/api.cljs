(ns api
  (:require
    ["fast-xml-parser" :as fxp]
    [clojure.string :refer [join]]
    [http :as h]
    [marshall :refer [marshall]]))

(def ^:private base-url "https://www.boardgamegeek.com/xmlapi2")

(defn- parse-xml [xml]
  (js->clj (.parse fxp xml #js{:ignoreAttributes false :attributeNamePrefix "$_"})))

(defn get-games [ids]
  (-> (str base-url "/thing?stats=1&type=boardgame,boardgameexpansion&id=" (join "," ids))
      h/get
      (.then #(as-> % $
                    (parse-xml $)
                    (get-in $ ["items" "item"])
                    (if (map? $)
                      [(marshall $)]
                      (map marshall $))
                    (clj->js $)))))

(defn get-plays [game-id page]
  (-> (str base-url "/plays?type=thing&subtype=boardgame&id=" game-id "&page=" page)
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

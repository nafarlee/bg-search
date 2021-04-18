(ns api
  (:require
    ["fast-xml-parser" :as fxp]
    [clojure.string :refer [join]]
    [http :as h]
    ["/marshall/index" :default marshall]))

(def ^:private base-url "https://api.geekdo.com/xmlapi2")

(defn- parse-xml [xml]
  (.parse fxp xml #js{:ignoreAttributes false}))

(defn get-games [ids]
  (-> (str base-url "/things?stats=1&type=boardgame,boardgameexpansion&id=" (join "," ids))
      h/get
      (.then parse-xml)
      (.then #(some-> %
                      (.. -items -item)
                      (.map marshall)))))

(defn get-plays [id page]
  (-> (str base-url "/plays?type=thing&subtype=boardgame&id=" id "&page=" page)
      h/get
      (.then parse-xml)
      (.then (fn [body]
               (as-> body $
                     (or (.. $ -plays -play) #js[])
                     (.map $ (fn [play]
                               #js[(js/parseInt (.. play -$ -id) 10)
                                   id
                                   (js/parseInt (.. play -$ -length) 10)
                                   (some-> play .-players first .-player .-length)])))))))

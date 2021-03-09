(ns api
  (:require
    ["util" :refer [promisify]]
    ["xml2js" :refer [parseString]]
    [clojure.string :refer [join]]
    ["/get" :default GET]
    ["/marshall/index" :default marshall]))

(def ^:private base-url "https://api.geekdo.com/xmlapi2")

(def ^:private parse-xml (promisify parseString))

(defn get-games [ids]
  (-> (str base-url "/things?stats=1&type=boardgame,boardgameexpansion&id=" (join ", " ids))
      GET
      (.then parse-xml)
      (.then #(some-> %
                      (.. -items -item)
                      (.map marshall)))))

(defn get-plays [id page]
  (-> (str base-url "/plays?type=thing&subtype=boardgame&id=" id "&page=" page)
      GET
      (.then parse-xml)
      (.then (fn [body]
               (as-> body $
                     (or (.. $ -plays -play) #js[])
                     (.map $ (fn [play]
                               #js[(.. play -$ -id)
                                   id
                                   (.. play -$ -length)
                                   (-> play .-players first .-player .-length)]))
                     (.filter $ (fn [[_ _ length]] (not= length "0"))))))))

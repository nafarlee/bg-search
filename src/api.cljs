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
      (.then #(.. % -items -item))
      (.then #(.map % marshall))))

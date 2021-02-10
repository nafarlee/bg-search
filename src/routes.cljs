(ns routes
  (:require
   [error :as err]
   [result :as rs]
   ["/transpile/index" :default transpile]))

(defn search [req res]
  (.set res "Cache-Control" (str "public, max-age=" (* 60 60 24 7)))
  (let [query     (or (.. req -query -query) "")
        order     (or (.. req -query -order) "bayes_rating")
        direction (or (.. req -query -direction) "DESC")
        offset    (-> (or (.. req -query -offset) 0) (js/parseInt 10))]
    (prn {:query query :order order :direction direction :offset offset})
    (let [sql-result (rs/attempt transpile query order direction offset)]
      (if (rs/error? sql-result)
        (err/transpile (rs/unwrap sql-result) res query)
        (.send res "Success")))))

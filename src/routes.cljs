(ns routes
  (:require
   ["url" :as url]
   ["/transpile/index" :default transpile]
   [promise :refer [then-not]]
   [error :as err]
   [result :as rs]))

(defn- next-url [req games]
   (url/format #js{:protocol (.-protocol req)
                   :host (.get req "host")
                   :pathname (.-path req)
                   :query (-> (.-query req)
                              js->clj
                              (update-in [:offset] + (count games))
                              clj->js)}))

(defn search [req res]
  (let [query      (or (.. req -query -query) "")
        order      (or (.. req -query -order) "bayes_rating")
        direction  (or (.. req -query -direction) "DESC")
        offset     (-> (.. req -query -offset) (or 0) (js/parseInt 10))
        database   (.-database req)
        sql-result (rs/attempt transpile query order direction offset)]
    (prn {:query query :order order :direction direction :offset offset})
    (if (rs/error? sql-result)
      (err/transpile (rs/unwrap sql-result) res query)
      (then-not (.query database (rs/unwrap sql-result))
        #(err/generic % res 500)
         (fn [db-response]
           (let [games (.-rows db-response)]
             (.render res
                      "search"
                      #js{:games games
                          :query query
                          :order order
                          :direction direction
                          :nextURL (next-url req games)})))))))

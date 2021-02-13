(ns routes
  (:require
   ["url" :as url]
   ["/transpile/index" :default transpile]
   [macro :refer [if-not-p]]
   [error :as err]
   [result :as rs]))

(defn- next-url [req games]
   (url/format #js{:protocol (.-protocol req)
                   :host     (.get req "host")
                   :pathname (.-path req)
                   :query    (-> (.-query req)
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
      (-> sql-result rs/unwrap (err/transpile res query) js/Promise.resolve)
      (if-not-p [[q-error q-result] (.query database (rs/unwrap sql-result))]
        (err/generic q-error res 500)
        (.render res "search" #js{:query     query
                                  :order     order
                                  :direction direction
                                  :games     (.-rows q-result)
                                  :nextURL   (next-url req (.-rows q-result))})))))

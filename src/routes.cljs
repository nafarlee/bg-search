(ns routes
  (:require
   ["url" :as url]
   ["pg" :refer [Client]]
   ["/transpile/index" :default transpile]
   [cljs.core.async :refer [go]]
   [cljs.core.async.interop :refer-macros [<p!]]
   [static :refer [credentials]]
   [error :as err]
   [result :as rs]))

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
        (go
          (let [client         (Client. credentials)
                connect-result (<p! (rs/from-promise (.connect client)))]
            (if (rs/error? connect-result)
              (do
                (.end client)
                (err/generic (rs/unwrap sql-result) res 500))
              (let [sql          (rs/unwrap sql-result)
                    query-result (<p! (rs/from-promise (.query client sql)))]
                (.end client)
                (if (rs/error? query-result)
                  (err/generic (rs/unwrap query-result) res 500)
                  (let [games          (-> query-result
                                           rs/unwrap
                                           .-rows)
                        next-url-query (-> (.-query req)
                                           js->clj
                                           (update-in [:offset] + (count games))
                                           clj->js)
                        next-url       (-> {:protocol (.-protocol req)
                                            :host (.get req "host")
                                            :pathname (.-path req)
                                            :query next-url-query}
                                           clj->js
                                           url/format)]
                    (.render res "search" (clj->js {:games games
                                                    :nextURL next-url
                                                    :query query
                                                    :order order
                                                    :direction direction}))))))))))))

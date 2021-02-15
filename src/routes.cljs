(ns routes
  (:require
   ["url" :as url]
   ["/transpile/index" :default transpile]
   [promise :refer [then-not]]
   [sql :as sql]
   [error :as err]
   [result :as rs]))

(defn games [req res]
  (let [database (.-database req)
        id       (.. req -params -id)]
    (then-not (sql/get-game database id)
      #(err/generic % res 500)
      (fn [game]
        (if-not game
          (err/generic (str "No game found with id '" id "'") res 404)
          (.render res "games" #js{:game game}))))))

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
      (then-not (.query database (rs/unwrap sql-result))
        #(err/generic % res 500)
        #(.render res "search" #js{:query     query
                                   :order     order
                                   :direction direction
                                   :games     (.-rows %)
                                   :nextURL   (next-url req (.-rows %))})))))

(ns core
  (:require
    [sql :as sql]
    ["express" :as express]
    [routes :as routes]
    [middleware :refer [with-database-pool with-header with-required-body-parameters]]))

(defonce ^:export app (express))

(defn main []
  (let [pool (delay (sql/pool))]
    (doto app
          (.use (.urlencoded express #js{:extended true}))
          (.get
           "/"
           (-> routes/index
               (with-header "Cache-Control" (str "public, max-age=" (* 60 60 24)))))
          (.use (.static express "public"))
          (.get
           "/image-mirror/:url(\\S+)"
           routes/image-mirror)
          (.get
           "/search"
           (with-database-pool pool)
           (-> routes/search
               (with-header "Cache-Control" (str "public, max-age=" (* 60 60 24 7)))))
          (.post
           "/pubsub/pull"
           (with-database-pool pool)
           routes/pull)
          (.post
           "/pubsub/pull-plays"
           (with-database-pool pool)
           routes/pull-plays)
          (.post
           "/pull-collection"
           (with-database-pool pool)
           (-> routes/pull-collection
               (with-required-body-parameters #{"username"})))
          (.get
           "/games/:id"
           (with-database-pool pool)
           (-> routes/games
               (with-header "Cache-Control" (str "public, max-age=" (* 60 60 24 7)))))
          (.listen 8080 #(prn "Listening on 8080...")))))

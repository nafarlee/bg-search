(ns core
  (:require
    ["express" :as express]
    [sql :as sql]
    [routes :as routes]
    [middleware :as middleware]))

(defonce ^:export app (express))

(defn main []
  (let [pool (delay (sql/pool))]
    (doto app
          (.use (.urlencoded express #js{:extended true}))
          (.get
           "/"
           (middleware/with-header "Cache-Control" (str "public, max-age=" (* 60 60 24)))
           routes/index)
          (.use (.static express "public"))
          (.get
           "/image-mirror/:url(\\S+)"
           routes/image-mirror)
          (.get
           "/search"
           (middleware/with-database-pool pool)
           (middleware/with-header "Cache-Control" (str "public, max-age=" (* 60 60 24 7)))
           middleware/with-query-params
           routes/search)
          (.post
           "/pubsub/pull"
           (middleware/with-database-pool pool)
           routes/pull)
          (.post
           "/pubsub/pull-plays"
           (middleware/with-database-pool pool)
           routes/pull-plays)
          (.post
           "/pull-collection"
           (middleware/with-required-body-parameters #{"username"})
           (middleware/with-database-pool pool)
           middleware/with-body
           routes/pull-collection)
          (.get
           "/games/:id"
           (middleware/with-database-pool pool)
           (middleware/with-header "Cache-Control" (str "public, max-age=" (* 60 60 24 7)))
           routes/games)
          (.listen 8080 #(prn "Listening on 8080...")))))

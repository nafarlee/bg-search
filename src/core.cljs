(ns core
  (:require
    ["express" :as express]
    [sql :as sql]
    [routes :as routes]
    [middleware :refer [with-database-pool with-header with-required-body-parameters]]))

(defonce ^:export app (express))

(defn main []
  (let [pool (delay (sql/pool))]
    (doto app
          (.use (.urlencoded express #js{:extended true}))
          (.get
           "/"
           (with-header "Cache-Control" (str "public, max-age=" (* 60 60 24)))
           routes/index)
          (.use (.static express "public"))
          (.get
           "/image-mirror/:url(\\S+)"
           routes/image-mirror)
          (.get
           "/search"
           (with-database-pool pool)
           (with-header "Cache-Control" (str "public, max-age=" (* 60 60 24 7)))
           routes/search)
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
           (with-required-body-parameters #{"username"})
           (with-database-pool pool)
           routes/pull-collection)
          (.get
           "/games/:id"
           (with-database-pool pool)
           (with-header "Cache-Control" (str "public, max-age=" (* 60 60 24 7)))
           routes/games)
          (.listen 8080 #(prn "Listening on 8080...")))))

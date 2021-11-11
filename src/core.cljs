(ns core
  (:require
    ["express" :as express]
    [view :as v]
    [sql :as sql]
    [routes :as r]
    [middleware :as m]))

(defonce ^:export app (express))

(defn main []
  (let [pool (delay (sql/pool))]
    (doto app
          (.use (.urlencoded express #js{:extended true}))

          (.get
           "/"
           (m/with-header "Cache-Control" (str "public, max-age=" (* 60 60 24)))
           (fn [_req res]
             (.send res (v/index))))

          (.use (.static express "public"))

          (.get
           "/image-mirror/:url(\\S+)"
           m/with-params
           m/with-image-mirror
           m/with-permanent-redirect)

          (.get
           "/search"
           (m/with-database-pool pool)
           (m/with-header "Cache-Control" (str "public, max-age=" (* 60 60 24 7)))
           m/with-query-params
           m/with-transpiled-query
           m/with-searched-games
           m/with-next-search-url
           r/search)

          (.post
           "/pubsub/pull"
           (m/with-database-pool pool)
           r/pull)

          (.post
           "/pubsub/pull-plays"
           (m/with-database-pool pool)
           r/pull-plays)

          (.post
           "/pull-collection"
           m/with-body
           (m/with-required-body-parameters #{:username})
           m/with-scraped-collection
           (m/with-database-pool pool)
           m/with-save-collection
           m/with-success)

          (.get
           "/games/:id"
           (m/with-database-pool pool)
           (m/with-header "Cache-Control" (str "public, max-age=" (* 60 60 24 7)))
           r/games)

          (.use m/log-error-cause)

          (.listen 8080 #(prn "Listening on 8080...")))))

(ns core
  (:require
    ["express" :as express]
    [routes :as routes]
    [middleware :refer [with-database with-header with-required-body-parameters]]))

(defonce ^:export app (express))

(defn main []
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
         (-> routes/search
             (with-header "Cache-Control" (str "public, max-age=" (* 60 60 24 7)))
             with-database))
        (.post
         "/pubsub/pull"
         (-> routes/pull
             with-database))
        (.post
         "/pubsub/pull-plays"
         (-> routes/pull-plays
             with-database))
        (.post
         "/pull-collection"
         (-> routes/pull-collection
             with-database
             (with-required-body-parameters #{"username"})))
        (.get
         "/games/:id"
         (-> routes/games
             (with-header "Cache-Control" (str "public, max-age=" (* 60 60 24 7)))
             with-database))
        (.listen 8080 #(prn "Listening on 8080..."))))

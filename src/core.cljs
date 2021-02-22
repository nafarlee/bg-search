(ns core
  (:require
    [static :refer [credentials]]
    ["express" :as express]
    [routes :as routes]
    [middleware :refer [with-database with-header]]
    ["/routes/pull-plays" :default pull-plays]
    ["/views/locals" :as locals]))

(defonce ^:export app (express))

(defn main []
  (set! (.-locals app)
        (js/Object.assign (.-locals app) locals))
  (doto app
        (.set "view engine" "pug")
        (.set "views" "src/views")
        (.use (.static express "public"))
        (.get "/search" (-> routes/search
                            (with-header "Cache-Control" (str "public, max-age=" (* 60 60 24 7)))
                            with-database))
        (.post "/pubsub/pull" (-> routes/pull
                                  with-database))
        (.post "/pubsub/pull-plays" (pull-plays credentials))
        (.get "/games/:id" (-> routes/games
                               (with-header "Cache-Control" (str "public, max-age=" (* 60 60 24 7)))
                               with-database))
        (.listen 8080 #(prn "Listening on 8080..."))))

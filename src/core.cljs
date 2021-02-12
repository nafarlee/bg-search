(ns core
  (:require
    [static :refer [credentials]]
    ["express" :as express]
    ["/routes/pull" :default pull]
    [routes :as routes]
    [middleware :refer [with-database]]
    ["/routes/pull-plays" :default pull-plays]
    ["/routes/games" :default games]
    ["/views/locals" :as locals]))

(defonce ^:export app (express))

(defn main []
  (set! (.-locals app)
        (js/Object.assign (.-locals app) locals))
  (doto app
        (.set "view engine" "pug")
        (.set "views" "src/views")
        (.use (.static express "public"))
        (.get "/search" (with-database routes/search))
        (.post "/pubsub/pull" (pull credentials))
        (.post "/pubsub/pull-plays" (pull-plays credentials))
        (.get "/games/:id" (games credentials))
        (.listen 8080 #(prn "Listening on 8080..."))))

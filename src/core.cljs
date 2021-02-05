(ns core
  (:require
    [shadow.resource :as rc]
    ["express" :as express]
    ["/routes/pull" :default pull]
    ["/routes/search" :default search]
    ["/routes/pull-plays" :default pull-plays]
    ["/routes/games" :default games]
    ["/views/locals" :as locals]))

(defonce ^:export app (express))

(defonce credentials (.parse js/JSON (rc/inline "/db-credentials.json")))

(defn main []
  (set! (.-locals app) (merge (.-locals app) locals))
  (doto app
        (.set "view engine" "pug")
        (.set "views" "src/views")
        (.use (.static express "public"))
        (.get "/search" (search credentials))
        (.get "/pubsub/pull" (pull credentials))
        (.get "/pubsub/pull-plays" (pull-plays credentials))
        (.get "/games/:id" (games credentials))
        (.listen 8080 #(prn "Listening on 8080..."))))

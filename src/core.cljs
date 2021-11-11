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
           m/with-search-page-number
           m/with-previous-search-url
           m/with-next-search-url
           (fn [^js req res]
             (let [{:keys [previous-url page-number next-url query searched-games]}  (.-locals req)
                   {:keys [direction order query]}                                   query]
               (.send res (v/search {:query        query
                                     :order        order
                                     :direction    direction
                                     :games        searched-games
                                     :previous-url previous-url
                                     :page-number  page-number
                                     :next-url     next-url})))))

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
           m/with-params
           m/with-game
           (fn [^js req res]
             (let [{:keys [game]} (.-locals req)]
               (.send res (v/games game)))))

          (.use m/log-error-cause)

          (.listen 8080 #(prn "Listening on 8080...")))))

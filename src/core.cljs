(ns core
  (:require
    ["express" :as express]
    [view :as v]
    [sql :as sql]
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
           (m/with-header "Cache-Control" (str "public, max-age=" (* 60 60 24)))
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
             (let [{:keys [previous-url
                           page-number
                           next-url
                           query
                           searched-games]}  (.-locals req)
                   {:keys [direction
                           order
                           limit
                           query]}           query]
               (.send res (v/search {:query        query
                                     :limit        limit
                                     :order        order
                                     :direction    direction
                                     :games        searched-games
                                     :previous-url previous-url
                                     :page-number  page-number
                                     :next-url     next-url})))))

          (.get
           "/pubsub/pull"
           (m/with-database-pool pool)
           m/with-game-checkpoint
           m/with-new-game-checkpoint
           m/with-pulled-games
           m/maybe-mobius
           m/with-game-insertions
           m/insert-games
           m/with-success)

          (.get
           "/pubsub/pull-plays"
           (m/with-database-pool pool)
           m/with-last-game
           m/with-plays-checkpoint
           m/maybe-mobius-plays
           m/require-play-id-game
           m/with-plays
           m/require-plays
           m/with-positive-plays
           m/require-positive-plays
           m/require-new-plays
           m/save-plays
           m/with-success)

          (.get
           "/scheduler/refresh-play-medians"
           (m/with-database-pool pool)
           (m/sql "REFRESH MATERIALIZED VIEW CONCURRENTLY play_medians")
           m/with-success)

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

          (.get
           "/admin/explain"
           (fn [^js _req res]
             (.send res (v/explain))))

          (.get
           "/admin/explain-results"
           (m/with-database-pool pool)
           m/with-query-params
           m/with-transpiled-query
           m/with-query-explanation
           (fn [^js req res]
             (let [{:keys [explanation query transpiled-query]} (.-locals req)
                   {:keys [query order direction offset]}       query]
               (.send res (v/explain-results {:explanation explanation
                                              :query       query
                                              :order       order
                                              :direction   direction
                                              :offset      offset
                                              :sql         transpiled-query})))))

          (.use m/log-error-cause)

          (.listen 8080 #(prn "Listening on 8080...")))))

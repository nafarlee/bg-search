(ns core
  (:require
    ["express" :as express]
    [view :as v]
    [sql :as sql]
    [middleware :as m]))

(defonce ^:export app (express))

(defn main []
  (assert (some? js/process.env.MIRROR_BASE_URL))
  (assert (some? js/process.env.BGG_API_KEY))
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
           "/cron/refresh-play-medians"
           (m/with-database-pool pool)
           (m/sql "REFRESH MATERIALIZED VIEW CONCURRENTLY play_medians")
           m/with-success)

          (.post
           "/pull-collection"
           m/with-body
           (m/with-required-body-parameters #{:username})
           (m/with-scraped-collection js/process.env.BGG_API_KEY)
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

          (.use m/log-error-cause))

    (let [server (.listen app 8080)]
      (println "Listening on 8080...")
      (.on js/process
           "SIGTERM"
           (fn []
             (println "SIGTERM received: closing HTTP server")
             (.close server
                     (fn []
                       (println "HTTP server closed")
                       (when (realized? pool)
                         (.end @pool)))))))))

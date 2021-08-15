(ns routes
  (:require
   ["url" :as url]
   [transpile :refer [transpile]]
   [sql.insert :refer [insert]]
   [sql :refer [query]]
   [promise :refer [then-not js-promise?]]
   [api :as api]
   [sql :as sql]
   [error :as err]
   [result :as rs]))

(defn success [res]
  (-> res
      (.status 200)
      .send))

(defn error [res status message js-error]
  (js/console.log js-error)
  (-> res
      (.status status)
      (.send message)))

(defn pull-plays [req res]
  (let [database           (.-database req)
        last-game-p        (sql/get-last-game database)
        plays-checkpoint-p (sql/get-plays-checkpoint database)]
    (then-not (js/Promise.all [last-game-p plays-checkpoint-p])
      #(err/generic % res 500)
      (fn [[last-game [play-id play-page]]]
        (if (> play-id last-game)
          (-> (sql/mobius-plays database)
              (.then #(prn :mobius-plays))
              (.then #(success res)))
          (then-not (sql/game? database play-id)
            #(err/generic % res 500)
            (fn [game?]
              (if-not game?
                (-> (sql/update-plays-checkpoint database (inc play-id) 1)
                    (.then #(prn :not-a-game play-id))
                    (.then #(success res)))
                (then-not (api/get-plays play-id play-page)
                  #(err/generic % res 500)
                  (fn [plays]
                    (let [positive-plays (filter (fn [[_ _ play-time]] (pos? play-time)) plays)]
                      (cond
                        (empty? plays)
                        (-> (sql/update-plays-checkpoint database (inc play-id) 1)
                            (.then #(prn :no-plays play-id play-page))
                            (.then #(success res)))

                        (empty? positive-plays)
                        (-> (sql/update-plays-checkpoint database play-id (inc play-page))
                            (.then #(prn :no-positive-plays play-id play-page))
                            (.then #(success res)))

                        :else
                        (then-not (sql/play? database (ffirst positive-plays))
                          #(err/generic % res 500)
                          (fn [play?]
                            (if play?
                              (-> (sql/update-plays-checkpoint database (inc play-id) 1)
                                  (.then #(prn :no-new-plays play-id play-page))
                                  (.then #(success res))
                                  (.catch #(err/generic % res 500)))
                              (-> (sql/save-plays database play-id play-page positive-plays)
                                  (.then #(prn :save-plays play-id play-page))
                                  (.then #(success res))
                                  (.catch #(err/generic % res 500))))))))))))))))))

(defn pull [req res]
  {:post [(js-promise? %)]}
  (let [database (.-database req)]
    (-> (sql/get-game-checkpoint database)
        (.then (fn [checkpoint]
                 (let [new-checkpoint (+ checkpoint 200)]
                   (-> (api/get-games (range checkpoint new-checkpoint))
                       (then-not
                         #(-> res (.status 500) .send)
                         (fn [games]
                           (if-not (seq games)
                             (-> (sql/mobius-games database)
                                 (.then #(prn :success :mobius-games))
                                 (.then #(-> res (.status 200) .send)))
                             (-> (sql/begin database)
                                 (.then #(sql/update-game-checkpoint database new-checkpoint))
                                 (.then #(->> games
                                              insert
                                              (map (partial query database))
                                              clj->js
                                              js/Promise.all))
                                 (.then #(sql/commit database))
                                 (.then #(prn :success checkpoint (dec new-checkpoint)))
                                 (.then #(-> res (.status 200) .send))
                                 (.catch (fn [error]
                                           (js/console.error error)
                                           (prn :error checkpoint (dec new-checkpoint))
                                           (-> (sql/rollback database)
                                               (.then #(-> res (.status 500) .send))))))))))))))))

(defn games [req res]
  (let [database (.-database req)
        id       (.. req -params -id)]
    (then-not (sql/get-game database id)
      #(err/generic % res 500)
      (fn [game]
        (if-not game
          (err/generic (str "No game found with id '" id "'") res 404)
          (.render res "games" #js{:game game}))))))

(defn- next-url [req games]
  (url/format #js{:protocol (.-protocol req)
                  :host     (.get req "host")
                  :pathname (.-path req)
                  :query    (js/Object.assign #js{}
                                              (.-query req)
                                              #js{:offset (-> req
                                                              (.. -query -offset)
                                                              (or "0")
                                                              (js/parseInt 10)
                                                              (+ (count games)))})}))

(defn search [req res]
  (let [query      (or (.. req -query -query) "")
        order      (or (.. req -query -order) "bayes_rating")
        direction  (or (.. req -query -direction) "DESC")
        offset     (-> (.. req -query -offset) (or 0) (js/parseInt 10))
        database   (.-database req)
        sql-result (rs/attempt transpile query order direction offset)]
    (prn {:query query :order order :direction direction :offset offset})
    (if (rs/error? sql-result)
      (-> sql-result rs/unwrap (err/transpile res query) js/Promise.resolve)
      (then-not (sql/query database (rs/unwrap sql-result))
        #(err/generic % res 500)
        #(.render res "search" #js{:query     query
                                   :order     order
                                   :direction direction
                                   :games     (.-rows %)
                                   :nextURL   (next-url req (.-rows %))})))))

(defn pull-collection [req res]
  (let [username (.. req -query -username)
        database (.-database req)]
    (-> (api/get-collection username)
        (.catch #(error res 500 "Could not get collection" %))
        (.then #(sql/save-collection database %))
        (.catch #(error res 500 "Could not save collection to database" %))
        (.then #(success res)))))

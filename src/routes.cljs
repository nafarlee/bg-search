(ns routes
  (:require
   [interop :refer [parse-int]]
   [view :as v]
   ["url" :as url]
   [transpile :refer [transpile]]
   [sql.insert :refer [insert]]
   [sql :refer [query] :as sql]
   [promise :refer [then-not js-promise?]]
   [api :as api]
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
  (let [database (.-database req)]
    (-> (js/Promise.all [(sql/get-last-game database) (sql/get-plays-checkpoint database)])
        (.then (fn [[last-game [play-id play-page]]]
                 (if (> play-id last-game)
                   (throw (ex-info "Mobius Plays" {} :mobius-plays))
                   {:play-id play-id :play-page play-page})))
        (.then (fn [{:keys [play-id] :as ctx}]
                 (.then (sql/game? database play-id)
                        #(assoc ctx :game? %))))
        (.then (fn [{:keys [game?] :as ctx}]
                 (if-not game?
                   (throw (ex-info "Not a Game" ctx :not-a-game))
                   ctx)))
        (.then (fn [{:keys [play-id play-page] :as ctx}]
                 (.then (api/get-plays play-id play-page)
                        #(assoc ctx :plays %))))
        (.then (fn [{:keys [plays] :as ctx}]
                 (if (empty? plays)
                   (throw (ex-info "No Plays" ctx :no-plays))
                   ctx)))
        (.then (fn [{:keys [plays] :as ctx}]
                 (assoc ctx
                        :positive-plays
                        (filter (fn [[_ _ play-time]] (pos? play-time)) plays))))
        (.then (fn [{:keys [positive-plays] :as ctx}]
                 (if (empty? positive-plays)
                   (throw (ex-info "No Positive Plays" ctx :no-positive-plays))
                   ctx)))
        (.then (fn [{:keys [positive-plays] :as ctx}]
                 (.then (sql/play? database (ffirst positive-plays))
                        #(assoc ctx :play? %))))
        (.then (fn [{:keys [play?] :as ctx}]
                 (if play?
                   (throw (ex-info "No New Plays" ctx :no-new-plays))
                   ctx)))
        (.then (fn [{:keys [play-id play-page positive-plays]}]
                (-> (sql/save-plays database play-id play-page positive-plays)
                    (.then #(prn :save-plays play-id play-page))
                    (.then #(success res)))))
        (.catch (fn [e]
                  (let [{:keys [play-id play-page]} (ex-data e)]
                    (case (ex-cause e)
                          :no-new-plays
                          (-> (sql/update-plays-checkpoint database (inc play-id) 1)
                              (.then #(prn :no-new-plays play-id play-page))
                              (.then #(success res)))

                          :no-positive-plays
                          (-> (sql/update-plays-checkpoint database play-id (inc play-page))
                              (.then #(prn :no-positive-plays play-id play-page))
                              (.then #(success res)))

                          :no-plays
                          (-> (sql/update-plays-checkpoint database (inc play-id) 1)
                              (.then #(prn :no-plays play-id play-page))
                              (.then #(success res)))

                          :mobius-plays
                          (-> (sql/mobius-plays database)
                              (.then #(prn :mobius-plays))
                              (.then #(success res)))

                          :not-a-game
                          (-> (sql/update-plays-checkpoint database (inc play-id) 1)
                              (.then #(prn :not-a-game play-id))
                              (.then #(success res)))

                          (err/generic e res 500))))))))

(defn pull [req res]
  {:post [(js-promise? %)]}
  (let [database (.-database req)]
    (-> (sql/get-game-checkpoint database)
        (.then #(hash-map :checkpoint % :new-checkpoint (+ 200 %)))
        (.then (fn [{:keys [checkpoint new-checkpoint] :as ctx}]
                 (.then (api/get-games (range checkpoint new-checkpoint))
                        #(assoc ctx :games %))))
        (.then (fn [{:keys [games] :as ctx}]
                 (if-not (seq games)
                   (throw (ex-info "Mobius Games" ctx :mobius-games))
                   ctx)))
        (.then (fn [{:keys [games] :as ctx}]
                 (assoc ctx :insertions (insert games))))
        (.then (fn [{:keys [insertions checkpoint new-checkpoint] :as ctx}]
                 (-> (sql/begin database)
                     (.then #(sql/update-game-checkpoint database new-checkpoint))
                     (.then #(js/Promise.all (map (partial query database) insertions)))
                     (.then #(sql/commit database))
                     (.then #(prn :save-games checkpoint (dec new-checkpoint)))
                     (.then #(success res))
                     (.catch #(throw (ex-info "Save Games Error" ctx :save-games-error))))))
        (.catch (fn [e]
                  (let [{:keys [checkpoint new-checkpoint]} (ex-data e)]
                    (case (ex-cause e)
                          :save-games-error
                          (-> (sql/rollback database)
                              (.then #(prn :save-games-error checkpoint (dec new-checkpoint)))
                              (.then #(err/generic (ex-message e) res 500)))

                          :mobius-games
                          (-> (sql/mobius-games database)
                              (.then #(prn :mobius-games))
                              (.then #(success res)))

                          (err/generic e res 500))))))))

(defn games [req res]
  (let [database (.-database req)
        id       (.. req -params -id)]
    (then-not (sql/get-game database id)
      #(err/generic % res 500)
      (fn [game]
        (if-not game
          (err/generic (str "No game found with id '" id "'") res 404)
          (.send res (v/games (js->clj game))))))))

(defn- next-url [req games]
  (let [query            (js->clj (.-query req))
        {:strs [offset]} query
        new-offset       (+ (parse-int (or offset "0"))
                            (count games))
        new-query        (clj->js (assoc query :offset new-offset))]
    (url/format #js{:host     (.get req "host")
                    :protocol (.-protocol req)
                    :pathname (.-path req)
                    :query    new-query})))

(defn- previous-url [req]
  (let [query (js->clj   (.-query req))
        {:strs [offset]} query]
    (when (and (string? offset) (not= "0" offset))
      (url/format #js{:host     (.get req "host")
                      :protocol (.-protocol req)
                      :pathname (.-path req)
                      :query    (clj->js (assoc query :offset (- (parse-int offset) 25)))}))))

(defn search [req res]
  (let [{:strs [query
                order
                direction
                offset]
         :or   {query ""
                order "bayes_rating"
                direction "DESC"
                offset "0"}
         :as   qp} (js->clj (.-query req))
        offset     (parse-int offset)]
    (prn qp)
    (-> (rs/attempt transpile query order direction offset)
        rs/->js-promise
        (.catch #(throw (ex-info "Could not transpile" {:error %} :transpile-error)))
        (.then #(sql/query (.-database req) %))
        (.then #(.send res (v/search {:query     query
                                      :order     order
                                      :direction direction
                                      :games     (js->clj (.-rows %))
                                      :next-url  (next-url req (.-rows %))})))
        (.catch #(case (ex-cause %)
                       :transpile-error (err/transpile (:error (ex-data %)) res query)
                       (err/generic (:error (ex-data %)) res 500))))))

(defn pull-collection [req res]
  (let [username (.. req -body -username)
        database (.-database req)]
    (-> (api/get-collection username)
        (.catch #(error res 500 "Could not get collection" %))
        (.then #(sql/save-collection database %))
        (.catch #(error res 500 "Could not save collection to database" %))
        (.then #(success res)))))

(defn index [req res]
  (.send res (v/index)))

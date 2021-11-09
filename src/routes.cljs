(ns routes
  (:require
   ["url" :as u]
   [image-mirror :as im]
   [interop :refer [parse-int]]
   [view :as v]
   [transpile :refer [transpile]]
   [sql.insert :refer [insert]]
   [sql :as sql]
   [promise :refer [then-not js-promise?]]
   [api :as api]
   [error :as err]
   [result :as rs]))

(defn error [res status message js-error]
  (js/console.log js-error)
  (-> res
      (.status status)
      (.send message)))

(defn pull-plays [^js req ^js res]
  (let [{:keys [database]} (.-locals req)]
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
                    (.then #(.sendStatus res 200)))))
        (.catch (fn [e]
                  (let [{:keys [play-id play-page]} (ex-data e)]
                    (case (ex-cause e)
                          :no-new-plays
                          (-> (sql/update-plays-checkpoint database (inc play-id) 1)
                              (.then #(prn :no-new-plays play-id play-page))
                              (.then #(.sendStatus res 200)))

                          :no-positive-plays
                          (-> (sql/update-plays-checkpoint database play-id (inc play-page))
                              (.then #(prn :no-positive-plays play-id play-page))
                              (.then #(.sendStatus res 200)))

                          :no-plays
                          (-> (sql/update-plays-checkpoint database (inc play-id) 1)
                              (.then #(prn :no-plays play-id play-page))
                              (.then #(.sendStatus res 200)))

                          :mobius-plays
                          (-> (sql/mobius-plays database)
                              (.then #(prn :mobius-plays))
                              (.then #(.sendStatus res 200)))

                          :not-a-game
                          (-> (sql/update-plays-checkpoint database (inc play-id) 1)
                              (.then #(prn :not-a-game play-id))
                              (.then #(.sendStatus res 200)))

                          (err/generic e res 500))))))))

(defn pull [^js req ^js res]
  {:post [(js-promise? %)]}
  (let [{:keys [database]} (.-locals req)]
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
                 (-> (sql/insert-games database insertions new-checkpoint)
                     (.then #(prn :save-games checkpoint (dec new-checkpoint)))
                     (.then #(.sendStatus res 200))
                     (.catch #(do (prn :save-games-error checkpoint (dec new-checkpoint))
                                  (err/generic % res 500))))))
        (.catch (fn [e]
                  (let [{:keys [checkpoint new-checkpoint]} (ex-data e)]
                    (case (ex-cause e)
                          :mobius-games
                          (-> (sql/mobius-games database)
                              (.then #(prn :mobius-games))
                              (.then #(.sendStatus res 200)))

                          (err/generic e res 500))))))))

(defn games [^js req res]
  (let [{:keys [database]} (.-locals req)
        id                 (.. req -params -id)]
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
    (u/format #js{:host     (.get req "host")
                    :protocol (.-protocol req)
                    :pathname (.-path req)
                    :query    new-query})))

(defn- previous-url [req]
  (let [query (js->clj   (.-query req))
        {:strs [offset]} query]
    (when (and (string? offset) (not= "0" offset))
      (u/format #js{:host     (.get req "host")
                      :protocol (.-protocol req)
                      :pathname (.-path req)
                      :query    (clj->js (assoc query :offset (- (parse-int offset) 25)))}))))

(defn- page-number [req]
  (let [offset (-> req
                   (.. -query -offset)
                   (or "0")
                   parse-int)]
    (inc (quot offset 25))))

(defn search [^js req res]
  (let [{:keys [database query]}         (.-locals req)
        {:strs [query
                order
                direction
                offset]
         :or   {query     ""
                order     "bayes_rating"
                direction "DESC"
                offset    "0"}
         :as   qp}                       query
        offset                           (parse-int offset)]
    (prn qp)
    (-> (rs/attempt transpile query order direction offset)
        rs/->js-promise
        (.catch #(throw (ex-info "Could not transpile" {:error %} :transpile-error)))
        (.then #(sql/query database %))
        (.then #(.send res (v/search {:query        query
                                      :order        order
                                      :direction    direction
                                      :games        (js->clj (.-rows %))
                                      :previous-url (previous-url req)
                                      :page-number  (page-number req)
                                      :next-url     (next-url req (.-rows %))})))
        (.catch #(case (ex-cause %)
                       :transpile-error (err/transpile (:error (ex-data %)) res query)
                       (do
                         (js/console.error %)
                         (err/generic (:error (ex-data %)) res 500)))))))

(defn pull-collection [^js req ^js res nxt]
  (let [{:keys [database collection]} (.-locals req)]
    (-> (sql/save-collection database collection)
        (.then #(.sendStatus res 200))
        (.catch nxt))))

(defn index [_req res]
  (.send res (v/index)))

(defn image-mirror [^js req res nxt]
  (let [{{:keys [url]} :params} (.-locals req)]
    (-> (im/serve #{"cf.geekdo-images.com"} url)
        (.then #(.redirect res 302 %))
        (.catch nxt))))

(ns middleware
  (:require
    ["url" :as u]
    [goog.object :as g]
    [clojure.string :refer [join]]
    [clojure.set :refer [difference]]
    [transpile :refer [transpile]]
    [interop :refer [js-error parse-int]]
    [error :as e]
    api
    sql
    [sql.insert :refer [insert]]
    [constants :refer [results-per-page]]))

(defn log-error-cause [^js err _req _res nxt]
  (when (.-cause err)
    (js/console.error #js{:cause (.-cause err)}))
  (nxt err))

(defn with-header [header value]
  (fn [_req res nxt]
    (.set res header value)
    (nxt)))

(defn with-required-body-parameters [required]
  (fn [^js req res nxt]
    (let [{:keys [body]} (.-locals req)
          actual         (-> body keys set)
          diff           (difference required actual)]
      (if (empty? diff)
        (nxt)
        (-> res
            (.status 422)
            (.send (str "Missing required query parameters: " (join ", " diff))))))))

(defn- assoc-locals! [^js req k v]
  (let [locals (or (.-locals req) {})]
    (set! (.-locals req) (assoc locals k v))))

(defn with-database-pool [pool]
  (fn [^js req _res nxt]
    (assoc-locals! req :database @pool)
    (nxt)))

(defn with-query-params [^js req _res nxt]
  (assoc-locals! req :query (js->clj (.-query req) :keywordize-keys true))
  (nxt))

(defn with-body [^js req _res nxt]
  (assoc-locals! req :body (js->clj (.-body req) :keywordize-keys true))
  (nxt))

(defn with-params [^js req _res nxt]
  (assoc-locals! req :params (js->clj (.-params req) :keywordize-keys true))
  (nxt))

(defn with-scraped-collection [^js req _res nxt]
  (let [{{:keys [username]} :body} (.-locals req)]
    (-> (api/get-collection username)
        (.then #(assoc-locals! req :collection %))
        (.then #(nxt))
        (.catch nxt))))

(defn with-save-collection [^js req _res nxt]
  (let [{:keys [database collection]} (.-locals req)]
    (-> (sql/save-collection database collection)
        (.then #(nxt))
        (.catch nxt))))

(defn with-success [_req ^js res]
  (.sendStatus res 200))

(defn with-transpiled-query [^js req res nxt]
  (let [{qp :query}                   (.-locals req)
        {:keys [query
                limit
                order
                direction
                offset]
         :or   {query ""
                limit (str results-per-page)
                order "bayes_rating"
                direction "DESC"
                offset "0"}}          qp
        offset                        (parse-int offset)]
    (prn qp)
    (try
      (assoc-locals! req :transpiled-query (transpile query order direction offset limit))
      (nxt)
      (catch :default err
        (e/transpile err res query)))))

(defn with-searched-games [^js req _res nxt]
  (let [{:keys [database transpiled-query]} (.-locals req)]
    (-> (sql/query database transpiled-query)
        (.then (fn [results]
                 (assoc-locals! req :searched-games (-> results .-rows js->clj))
                 (nxt)))
        (.catch nxt))))

(defn with-next-search-url [^js req _res nxt]
  (let [{:keys [query searched-games]}    (.-locals req)
        {:keys [offset]
         :or   {offset "0"}}              query
        new-offset                        (+ (parse-int offset) (count searched-games))
        new-query                         (clj->js (assoc query :offset new-offset))
        next-url                          (u/format #js{:host     (.get req "host")
                                                        :protocol (.-protocol req)
                                                        :pathname (.-path req)
                                                        :query    new-query})]
    (assoc-locals! req :next-url next-url)
    (nxt)))

(defn with-previous-search-url [^js req _res nxt]
  (let [{:keys [query]}  (.-locals req)
        {:keys [limit offset]} query]
    (when (and (string? offset) (not= "0" offset))
      (assoc-locals! req
                     :previous-url
                     (u/format #js{:host     (.get req "host")
                                   :protocol (.-protocol req)
                                   :pathname (.-path req)
                                   :query    (->> (- (parse-int offset) limit)
                                                  (assoc query :offset)
                                                  clj->js)}))))
  (nxt))

(defn with-search-page-number [^js req _res nxt]
  (let [{{:keys [offset limit]
          :or   {offset "0"
                 limit  results-per-page}} :query} (.-locals req)]
    (assoc-locals! req :page-number (-> offset
                                        parse-int
                                        (quot limit)
                                        inc)))
  (nxt))

(defn with-game [^js req _res nxt]
  (let [{:keys [database params]} (.-locals req)
        {:keys [id]}              params]
    (-> (sql/get-game database id)
        (.then (fn [game]
                 (if-not game
                   (nxt (js-error (str "No game found with ID " id)))
                   (do
                     (assoc-locals! req :game (js->clj game))
                     (nxt)))))
        (.catch nxt))))

(defn with-game-checkpoint [^js req _res nxt]
  (let [{:keys [database]} (.-locals req)]
    (-> (sql/get-game-checkpoint database)
        (.then (fn [checkpoint]
                 (assoc-locals! req :checkpoint checkpoint)
                 (nxt)))
        (.catch nxt))))

(defn with-new-game-checkpoint [^js req _res nxt]
  (let [{:keys [checkpoint]} (.-locals req)]
    (assoc-locals! req :new-checkpoint (+ 20 checkpoint)))
  (nxt))

(defn with-pulled-games [^js req _res nxt]
  (let [{:keys [checkpoint new-checkpoint]} (.-locals req)]
    (-> (api/get-games (range checkpoint new-checkpoint))
        (.then (fn [games]
                 (assoc-locals! req :games games)
                 (nxt)))
        (.catch nxt))))

(defn maybe-mobius [^js req ^js res nxt]
  (let [{:keys [database new-checkpoint]} (.-locals req)]
    (if (< new-checkpoint 460000)
      (nxt)
      (-> (sql/mobius-games database)
          (.then (fn []
                   (prn :mobius-games)
                   (.sendStatus res 200)))
          (.catch nxt)))))

(defn with-game-id-cliff [^js req _res nxt]
  (let [{:keys [database]} (.-locals req)]
    (-> (sql/get-game-id-cliff database)
        (.then (fn [game-id-cliff]
                 (assoc-locals! req :game-id-cliff game-id-cliff)
                 (nxt)))
        (.catch nxt))))

(defn with-game-insertions [^js req _res nxt]
  (let [{:keys [games]} (.-locals req)]
    (assoc-locals! req :insertions (insert games)))
  (nxt))

(defn insert-games [^js req _res nxt]
  (let [{:keys [database insertions checkpoint new-checkpoint]} (.-locals req)]
    (-> (sql/insert-games database insertions new-checkpoint)
        (.then (fn []
                 (prn :save-games checkpoint (dec new-checkpoint))
                 (nxt)))
        (.catch (fn [e]
                  (prn :save-games-error checkpoint (dec new-checkpoint))
                  (nxt e))))))

(defn with-query-explanation [^js req _res nxt]
  (let [{:keys [database transpiled-query]} (.-locals req)]
    (-> (sql/query database (update transpiled-query :text #(concat ["explain" "analyze"] %)))
        (.then (fn [result]
                 (as-> result <>
                       (.-rows <>)
                       (.map <> #(g/get % "QUERY PLAN"))
                       (join "\n" <>)
                       (assoc-locals! req :explanation <>)
                       (nxt)))))))

(defn with-last-game [^js req res nxt]
  (let [{:keys [database]} (.-locals req)]
    (-> (sql/get-last-game database)
        (.then (fn [last-game]
                 (assoc-locals! req :last-game last-game)
                 (nxt)))
        (.catch nxt))))

(defn with-plays-checkpoint [^js req _res nxt]
  (let [{:keys [database]} (.-locals req)]
    (-> (sql/get-plays-checkpoint database)
        (.then (fn [[play-id play-page]]
                 (assoc-locals! req :play-checkpoint {:play-id play-id :play-page play-page})
                 (nxt)))
        (.catch nxt))))

(defn maybe-mobius-plays [^js req ^js res nxt]
  (let [{:keys [last-game play-checkpoint database]} (.-locals req)
        {:keys [play-id]}                            play-checkpoint]
    (if (< play-id last-game)
      (nxt)
      (-> (sql/mobius-plays database)
          (.then (fn []
                   (prn :mobius-plays)
                   (.sendStatus res 200)))
          (.catch nxt)))))

(defn require-play-id-game [^js req ^js res nxt]
  (let [{database          :database
         {:keys [play-id]} :play-checkpoint} (.-locals req)]
    (-> (sql/game? database play-id)
        (.then (fn [game?]
                 (if game?
                   (nxt)
                   (-> (sql/update-plays-checkpoint database (inc play-id) 1)
                       (.then #(prn :not-a-game play-id))
                       (.then #(.sendStatus res 200))))))
        (.catch nxt))))

(defn with-plays [^js req _res nxt]
  (let [{{:keys [play-id play-page]} :play-checkpoint} (.-locals req)]
    (-> (api/get-plays play-id play-page)
        (.then (fn [plays]
                 (assoc-locals! req :plays plays)
                 (nxt)))
        (.catch nxt))))

(defn require-plays [^js req ^js res nxt]
  (let [{:keys [plays database play-checkpoint]} (.-locals req)
        {:keys [play-id play-page]}              play-checkpoint]
    (if (not-empty plays)
      (nxt)
      (-> (sql/update-plays-checkpoint database (inc play-id) 1)
          (.then (fn []
                   (prn :no-plays play-id play-page)
                   (.sendStatus res 200)))
          (.catch nxt)))))

(defn with-positive-plays [^js req _res nxt]
  (let [{:keys [plays]} (.-locals req)]
    (assoc-locals! req
                   :positive-plays
                   (filter (fn [[_ _ play-time]] (pos? play-time))
                           plays)))
  (nxt))

(defn require-positive-plays [^js req ^js res nxt]
  (let [{:keys [database play-checkpoint positive-plays]} (.-locals req)
        {:keys [play-id play-page]}                       play-checkpoint]
    (if (not-empty positive-plays)
      (nxt)
      (-> (sql/update-plays-checkpoint database play-id (inc play-page))
          (.then (fn []
                   (prn :no-positive-plays play-id play-page)
                   (.sendStatus res 200)))
          (.catch nxt)))))

(defn require-new-plays [^js req ^js res nxt]
  (let [{:keys [database play-checkpoint positive-plays]} (.-locals req)
        {:keys [play-id play-page]}                       play-checkpoint]
    (-> (sql/play? database (ffirst positive-plays))
        (.then (fn [existing-play?]
                 (if-not existing-play?
                   (nxt)
                   (-> (sql/update-plays-checkpoint database (inc play-id) 1)
                       (.then (fn []
                                (prn :no-new-plays play-id play-page)
                                (.sendStatus res 200)))))))
        (.catch nxt))))

(defn save-plays [^js req _res nxt]
  (let [{:keys [database play-checkpoint positive-plays]} (.-locals req)
        {:keys [play-id play-page]}                       play-checkpoint]
    (-> (sql/save-plays database play-id play-page positive-plays)
        (.then (fn []
                 (prn :save-plays play-id play-page)
                 (nxt)))
        (.catch nxt))))

(defn sql [text]
  (fn [^js req _res nxt]
    (let [{:keys [database]} (.-locals req)]
      (-> (.query database text)
          (.then #(nxt))
          (.catch nxt)))))

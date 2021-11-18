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
    [image-mirror :as im]))

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

(defn with-image-mirror [^js req _res nxt]
  (let [{{:keys [url]} :params} (.-locals req)]
    (-> (im/serve #{"cf.geekdo-images.com"} url)
        (.then (fn [redirect-url]
                 (assoc-locals! req :redirect-url redirect-url)
                 (nxt)))
        (.catch nxt))))

(defn with-success [_req ^js res]
  (.sendStatus res 200))

(defn with-permanent-redirect [^js req res]
  (let [{:keys [redirect-url]} (.-locals req)]
    (.redirect res 301 redirect-url)))

(defn with-transpiled-query [^js req res nxt]
  (let [{qp :query}                   (.-locals req)
        {:keys [query
                order
                direction
                offset]
         :or   {query ""
                order "bayes_rating"
                direction "DESC"
                offset "0"}}          qp
        offset                        (parse-int offset)]
    (prn qp)
    (try
      (assoc-locals! req :transpiled-query (transpile query order direction offset))
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
        {:keys [offset]} query]
    (when (and (string? offset) (not= "0" offset))
      (assoc-locals! req
                     :previous-url
                     (u/format #js{:host     (.get req "host")
                                   :protocol (.-protocol req)
                                   :pathname (.-path req)
                                   :query    (->> (- (parse-int offset) 25)
                                                  (assoc query :offset)
                                                  clj->js)}))))
  (nxt))

(defn with-search-page-number [^js req _res nxt]
  (let [{{:keys [offset]
          :or   {offset "0"}} :query} (.-locals req)]
    (assoc-locals! req :page-number (-> offset
                                        parse-int
                                        (quot 25)
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
    (assoc-locals! req :new-checkpoint (+ 200 checkpoint)))
  (nxt))

(defn with-pulled-games [^js req _res nxt]
  (let [{:keys [checkpoint new-checkpoint]} (.-locals req)]
    (-> (api/get-games (range checkpoint new-checkpoint))
        (.then (fn [games]
                 (assoc-locals! req :games games)
                 (nxt)))
        (.catch nxt))))

(defn maybe-mobius [^js req ^js res nxt]
  (let [{:keys [games database]} (.-locals req)]
    (if (seq games)
      (nxt)
      (-> (sql/mobius-games database)
          (.then (fn []
                   (prn :mobius-games)
                   (.sendStatus res 200)))
          (.catch nxt)))))

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

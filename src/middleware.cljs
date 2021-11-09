(ns middleware
  (:require
    api
    sql
    [clojure.string :refer [join]]
    [clojure.set :refer [difference]]))

(defn with-error-handler [^js err _req ^js res _nxt]
  (js/console.error err)
  (when (.-cause err)
    (js/console.error #js{:cause (.-cause err)}))
  (.sendStatus res 500))

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

(defn with-save-collection [req _res nxt]
  (let [{:keys [database collection]} (.-locals req)]
    (-> (sql/save-collection database collection)
        (.then #(nxt))
        (.catch nxt))))

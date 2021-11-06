(ns middleware
  (:require
    api
    [clojure.string :refer [join]]
    [clojure.set :refer [difference]]))

(defn with-error-handler [^js err _req ^js res _nxt]
  (js/console.error err)
  (js/console.error #js{:cause (.-cause err)})
  (-> res
      (.status 500)
      (.send "Internal Server Error")))

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
  (assoc-locals! req :query (js->clj (.-query req)))
  (nxt))

(defn with-body [^js req _res nxt]
  (assoc-locals! req :body (js->clj (.-body req)))
  (nxt))

(defn with-scraped-collection [^js req ^js res nxt]
  (let [{:keys [body]}     (.-locals req)
        {:strs [username]} body]
    (-> (api/get-collection username)
        (.then #(assoc-locals! req :collection %))
        (.then #(nxt))
        (.catch #(.sendStatus res 500)))))

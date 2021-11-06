(ns middleware
  (:require
    [clojure.string :refer [join]]
    [clojure.set :refer [difference]]))

(defn with-header [header value]
  (fn [_req res nxt]
    (.set res header value)
    (nxt)))

(defn with-required-body-parameters [required]
  (fn [req res nxt]
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

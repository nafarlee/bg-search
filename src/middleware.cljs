(ns middleware
  (:require
    [clojure.string :refer [join]]
    [clojure.set :refer [difference]]))

(defn with-header [handler header value]
  (fn [req res]
    (.set res header value)
    (handler req res)))

(defn with-required-body-parameters [handler required]
  (fn [req res]
    (let [qps  (set (keys (js->clj (.-body req))))
          diff (difference required qps)]
      (if (= diff #{})
        (handler req res)
        (-> res
            (.status 422)
            (.send (str "Missing required query parameters: "
                        (join ", " diff))))))))

(defn with-database-pool [pool]
  (fn [req _res nxt]
    (set! (.-database req) @pool)
    (nxt)))

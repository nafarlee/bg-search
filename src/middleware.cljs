(ns middleware
  (:require
    [clojure.string :refer [join]]
    [clojure.set :refer [difference]]))

(defn with-header [header value]
  (fn [_req res nxt]
    (.set res header value)
    (nxt)))

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

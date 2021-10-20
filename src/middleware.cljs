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
    (let [actual  (-> req .-body js->clj keys set)
          diff    (difference required actual)]
      (if (empty? diff)
        (nxt)
        (-> res
            (.status 422)
            (.send (str "Missing required query parameters: " (join ", " diff))))))))

(defn with-database-pool [pool]
  (fn [req _res nxt]
    (set! (.-database req) @pool)
    (nxt)))

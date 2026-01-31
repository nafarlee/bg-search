(ns http
 (:require
  [promise :refer [wait]]))

(defn fetch
  ([url]         (fetch url {}))
  ([url options] (js/fetch url (clj->js options))))

(defn- backoff [failed-attempt-count]
  (when (<= failed-attempt-count 5)
    (rand-int 30000)))

(defn fetch-with-backoff
  ([url] (fetch-with-backoff url {} backoff 0))
  ([url options] (fetch-with-backoff url options backoff 0))
  ([url options backoff-fn] (fetch-with-backoff url options backoff-fn 0))
  ([url options backoff-fn attempt]
   (-> (fetch url options)
       (.then (fn [response]
                (let [backoff (backoff-fn attempt)]
                  (if (or (not= 429 response.status)
                          (nil? backoff))
                    response
                    (do
                      (println "Waiting" backoff "ms...")
                      (-> (wait backoff)
                          (.then #(fetch-with-backoff url
                                                      options
                                                      backoff-fn
                                                      (inc attempt))))))))))))

(defn map->params [m]
  (-> m
      clj->js
      js/URLSearchParams.
      .toString))

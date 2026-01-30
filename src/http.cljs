(ns http
  (:refer-clojure :exclude [get])
  (:require
    [clojure.math :refer [pow]]
    ["https" :as js-https]))

(defn- success? [code]
  (and (>= code 200) (< code 300)))

(defn- handle-response [on-success ^js res]
  (let [chunks #js[]
        cb     #(on-success {:body % :status (.-statusCode res)})]
    (doto res
          (.on "data" #(.push chunks %))
          (.on "end" #(-> chunks
                          js/Buffer.concat
                          .toString
                          cb)))))

(defn sleep [duration]
  (js/Promise.
   (fn [fulfill]
     (js/setTimeout fulfill duration))))

(defn- rate-limit [duration f]
  (let [timer (atom (js/Promise.resolve))]
    (fn [& args]
      (let [return (.then @timer #(apply f args))]
        (reset! timer (.finally return #(sleep duration)))
        return))))

(def get
  (rate-limit 10000
    (fn [url]
      (js/Promise.
       (fn [fulfill reject]
         (-> (js-https/get url
                           (clj->js
                            {:headers
                             {:Authorization
                              (str "Bearer " js/process.env.BGG_API_KEY)}})
                           (partial handle-response fulfill))
             (.on "error" reject)))))))

(defn fetch
  ([url]         (fetch url {}))
  ([url options] (js/fetch url (clj->js options))))

(defn fetch-with-backoff
  ([url] (fetch-with-backoff url {}))
  ([url options] (fetch-with-backoff url
                                     options
                                     (fn [attempt]
                                       (when (<= attempt 5)
                                         (+ 10000 (* 5000 attempt))))
                                     0))
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
                      (-> (sleep backoff)
                          (.then #(fetch-with-backoff url
                                                      options
                                                      backoff-fn
                                                      (inc attempt))))))))))))

(defn map->params [m]
  (-> m
      clj->js
      js/URLSearchParams.
      .toString))

(defn unwrap [{:keys [status body] :as response}]
  (if (success? status)
    body
    (throw response)))

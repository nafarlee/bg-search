(ns http
  (:refer-clojure :exclude [get])
  (:require
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

(defn get [url]
  (js/Promise.
   (fn [fulfill reject]
     (-> (js-https/get url (partial handle-response fulfill))
         (.on "error" reject)))))

(defn unwrap [{:keys [status body] :as response}]
  (if (success? status)
    body
    (throw response)))

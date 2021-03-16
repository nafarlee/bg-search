(ns http
  (:require
    ["https" :as js-https]))

(defn- success? [code]
  (and (>= code 200) (< code 300)))

(defn- collect-body [res cb]
  (let [body #js[]]
    (-> res
        (.on "data" #(.push body %))
        (.on "end" #(cb (.toString (.concat js/Buffer body)))))
    nil))

(defn get [url]
  (js/Promise.
   (fn [fulfill reject]
     (-> (js-https/get url
                       (fn [res]
                         (if-not (success? (.-statusCode res))
                           (reject (.-statusCode res))
                           (collect-body res fulfill))))
          (.on "error" reject)))))

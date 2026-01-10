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
  (rate-limit 5000
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

(defn unwrap [{:keys [status body] :as response}]
  (if (success? status)
    body
    (throw response)))

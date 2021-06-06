(ns http
  (:require
    ["https" :as js-https]))

(defn- success? [code]
  (and (>= code 200) (< code 300)))

(defn- handle-response [on-success on-error res]
  (let [chunks      #js[]
        status-code (.-statusCode res)
        cb          (if (success? status-code) on-success on-error)]
    (doto res
          (.on "data" #(.push chunks %))
          (.on "end" #(-> chunks
                          js/Buffer.concat
                          .toString
                          cb)))))

(defn get [url]
  (js/Promise.
   (fn [fulfill reject]
     (-> (js-https/get url (partial handle-response fulfill reject))
         (.on "error" reject)))))

(ns image-mirror
  (:require
    ["https" :as https]
    ["util" :as util]
    ["stream" :as stream]))

(def pipeline (util/promisify stream/pipeline))

(defn download-stream [url]
  (js/Promise.
   (fn [fulfill reject]
     (https/get
      url
      (fn [res]
        (if (== 200 (.-statusCode res))
          (fulfill res)
          (reject res)))))))

(ns image-mirror
  (:require
    ["crypto" :as crypto]
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

(defn md5 [string]
  (-> (crypto/createHash "md5")
      (.update string)
      (.digest "hex")
      .toString))

(ns image-mirror
  (:require
    ["path" :as path]
    ["crypto" :as crypto]
    ["https" :as https]
    ["util" :as util]
    ["stream" :as stream]
    ["@google-cloud/storage" :as gcs]))

(def bucket (-> (gcs/Storage.)
                (.bucket "bg-search-images")))

(def ^:private pipeline (util/promisify stream/pipeline))

(defn- download-stream [url]
  (js/Promise.
   (fn [fulfill reject]
     (https/get
      url
      (fn [res]
        (if (== 200 (.-statusCode res))
          (fulfill res)
          (reject res)))))))

(defn- md5 [string]
  (-> (crypto/createHash "md5")
      (.update string)
      (.digest "hex")
      .toString))

(defn- download [url write-stream]
  (-> (download-stream url)
      (.then #(pipeline % write-stream))))

(defn serve [url]
  (let [filename (str (md5 url) (path/extname url))
        gcs-file (.file bucket filename)]
    (-> (.exists gcs-file)
        (.then (fn [[exists?]]
                 (when-not exists?
                   (prn :cache-miss)
                   (download url (.createWriteStream gcs-file)))))
        (.then #(.publicUrl gcs-file)))))

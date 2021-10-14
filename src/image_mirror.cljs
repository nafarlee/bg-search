(ns image-mirror
  (:require
    [clojure.string :as s]
    ["crypto" :as crypto]
    ["https" :as https]
    ["util" :as util]
    ["fs" :as fs]
    ["stream" :as stream]))

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

(defn serve [url]
  (let [path (str "public/" (md5 url) "." (last (s/split url ".")))]
    (-> (download-stream url)
        (.then #(pipeline % (fs/createWriteStream path)))
        (.then (constantly path)))))

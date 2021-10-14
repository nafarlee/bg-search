(ns image-mirror
  (:require
    [clojure.string :as s]
    ["crypto" :as crypto]
    ["https" :as https]
    ["util" :as util]
    ["fs" :as fs]
    ["stream" :as stream]))

(def ^:private pipeline (util/promisify stream/pipeline))

(def ^:private mkdir (util/promisify fs/mkdir))

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
  (let [filename (str (md5 url) "." (last (s/split url ".")))
        folder   "public/image/"
        path     (str folder filename)]
    (-> (mkdir folder #js{:recursive true})
        (.then #(download-stream url))
        (.then #(pipeline % (fs/createWriteStream path)))
        (.then (constantly filename)))))

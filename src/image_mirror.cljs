(ns image-mirror
  (:require
    ["path" :as path]
    ["crypto" :as crypto]
    ["https" :as https]
    ["util" :as util]
    ["fs" :as fs]
    ["stream" :as stream]))

(def ^:private pipeline (util/promisify stream/pipeline))

(def ^:private mkdir (util/promisify fs/mkdir))

(def ^:private access (util/promisify fs/access))

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

(defn- download-local [url filepath]
  (-> (mkdir (path/dirname filepath) #js{:recursive true})
      (.then #(download url (fs/createWriteStream filepath)))))

(defn serve [url]
  (let [filename (str (md5 url) (path/extname url))
        filepath (path/join "public/image" filename)]
    (-> (access filepath)
        (.catch #(download-local url filepath))
        (.then (constantly filename)))))

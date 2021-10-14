(ns image-mirror
  (:require
    ["util" :as util]
    ["stream" :as stream]))

(def pipeline (util/promisify stream/pipeline))

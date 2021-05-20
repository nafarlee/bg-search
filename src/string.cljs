(ns string
  (:require
    [clojure.string :as s]))

(defn snake->kebab [string]
  (s/replace string \_ \-))

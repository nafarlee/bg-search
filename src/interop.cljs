(ns interop)

(defn parse-int
  ([s b] (js/ParseInt s b))
  ([s] (parse-int s 10)))

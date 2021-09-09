(ns interop)

(defn parse-int
  ([s b] (js/parseInt s b))
  ([s] (parse-int s 10)))

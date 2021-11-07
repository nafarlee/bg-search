(ns interop)

(defn parse-int
  ([s b] (js/parseInt s b))
  ([s] (parse-int s 10)))

(defn js-error
  ([message] (js/Error. message))
  ([message cause] (js/Error. message #js{:cause cause})))

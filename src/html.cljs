(ns html)

(defn render [[tag attributes & body]]
  (if-not (map? attributes)
    (recur (concat [tag {} attributes] body))
    (str "<"
         (name tag)
         (reduce (fn [acc [k v]] (str acc " " (name k) "=\"" v "\""))
                 ""
                 attributes)
         ">"
         (reduce (fn [acc b]
                   (if (string? b)
                     (str acc b)
                     (str acc (render b))))
                 ""
                 body)
         "</"
         (name tag)
         ">")))

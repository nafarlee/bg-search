(ns html)

(defn clj->html [[tag attributes & body]]
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
                     (str acc (clj->html b))))
                 ""
                 body)
         "</"
         (name tag)
         ">")))

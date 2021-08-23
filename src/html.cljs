(ns html)

(defn render-attributes [attributes]
  (reduce (fn [acc [k v]]
            (str acc " " (name k) "=\"" v "\""))
          ""
          attributes))

(declare clj->html)

(defn render-element [[tag attributes & body]]
  (if-not (map? attributes)
    (recur (concat [tag {} attributes] body))
    (str
     "<"
     (name tag)
     (render-attributes attributes)
     ">"
     (apply str (map clj->html body))
     "</"
     (name tag)
     ">")))

(defn clj->html [element]
  (if-not (vector? element)
    (str element)
    (render-element element)))

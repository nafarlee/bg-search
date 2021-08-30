(ns html)

(defn render-attributes [attributes]
  (reduce (fn [acc [k v]]
            (str acc " " (name k) "=\"" v "\""))
          ""
          attributes))

(declare html)

(defn render-element [[tag attributes & body]]
  (if-not (map? attributes)
    (recur (concat [tag {} attributes] body))
    (str
     "<"
     (name tag)
     (render-attributes attributes)
     ">"
     (apply str (map html body))
     "</"
     (name tag)
     ">")))

(defn html [element]
  (cond
    (vector? element) (render-element element)
    (seq? element)    (apply str (map html element))
    :else             (str element)))

(def doctype "<!DOCTYPE html>")

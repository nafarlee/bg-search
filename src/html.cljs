(ns html
  (:require
   [clojure.string :refer [join replace split trim]]))

(defn escape [h]
  (-> h
      (replace "&" "&amp;")
      (replace "<" "&lt;")
      (replace ">" "&gt;")
      (replace "\"" "&quot;")
      (replace "'" "&apos;")))

(defn render-attributes [attributes]
  (reduce (fn [acc [k v]]
            (if v
              (str acc " " (name k) "=\"" v "\"")
              acc))
          ""
          attributes))

(declare html)

(defn parse-tag-keyword [kw]
  (let [s               (name kw)
        [tag & classes] (split s #"\.")]
    {:tag   tag
     :class (when classes (join " " classes))}))

(defn render-element [[kw attributes & body]]
  (if-not (map? attributes)
    (recur (concat [kw {} attributes] body))
    (let [{:keys [tag class]} (parse-tag-keyword kw)
          merged-attributes   (update attributes
                                      :class
                                      (fn [explicit-class]
                                        (when (or class explicit-class)
                                          (trim (str class " " explicit-class)))))]
      (str
       "<"
       tag
       (render-attributes merged-attributes)
       ">"
       (apply str (map html body))
       "</"
       tag
       ">"))))

(defn html [element]
  (cond
    (vector? element) (render-element element)
    (seq? element)    (apply str (map html element))
    :else             (str element)))

(def doctype "<!DOCTYPE html>")

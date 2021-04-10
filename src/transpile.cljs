(ns transpile
  (:require
    [clojure.string :as s]
    ["/language/index" :default lang]
    ["/transpile/lib" :default tl]
    ["/transpile/index" :as t]))

(defn- create-generator [s]
  (let [remaining (atom s)]
    (fn []
      (let [f (first @remaining)]
        (swap! remaining rest)
        f))))

(def ^:private parameter-templates (map #(str "$" (inc %)) (range)))

(defn transpile [query order direction offset]
  {:pre [(some (partial = order)
               (.-FIELDS tl))
         (#{"ASC" "DESC"} direction)]}
  (let [ast                   (.tryParse lang query)
        {:strs [text values]} (js->clj (.toSQL t ast))]
    #js{:values (conj values offset)
        :text   (as-> text $
                      (if (empty? $)
                        "games"
                        (str "(" $ ") AS GameSubquery NATURAL INNER JOIN games"))
                      (str "SELECT DISTINCT " (.-CONCATENATED_FIELDS tl)
                           " FROM " $
                           " ORDER BY " order " " direction
                           " LIMIT 25 OFFSET {{}}")
                      (s/replace $ #"\{\{\}\}" (create-generator parameter-templates)))}))

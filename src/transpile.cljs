(ns transpile
  (:require
    [clojure.string :as s]
    ["/language/index" :default lang]
    ["/transpile/lib" :default tl]
    ["/transpile/index" :as t]))

(defn generator [s]
  (let [remaining (atom s)]
    (fn []
      (let [f (first @remaining)]
        (swap! remaining rest)
        f))))

(defn transpile [query order direction offset]
  {:pre [(some (partial = order) (.-FIELDS tl))
         (#{"ASC" "DESC"} direction)]}
  (let [predicates            (.tryParse lang query)
        {:strs [text values]} (js->clj (.toSQL t predicates))
        from                  (if (empty? text)
                                "games"
                                (str "(" text ") AS GameSubquery NATURAL INNER JOIN games"))
        text                  (str "SELECT DISTINCT " (.-CONCATENATED_FIELDS tl) " "
                                   "FROM " from " "
                                   "ORDER BY " order " " direction " "
                                   "LIMIT 25 OFFSET {{}}")
        values                (conj values offset)
        yield                 (generator (map #(str "$" (inc %)) (range)))]
    #js{:text (s/replace text #"\{\{\}\}" yield)
        :values values}))

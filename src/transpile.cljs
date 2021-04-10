(ns transpile
  (:require
    [goog.object :as g]
    [clojure.string :as s]
    ["/language/index" :default lang]
    ["/transpile/lib" :default tl]))

(defn simple [field params]
  (let [value  (.-value params)
        prefix (if (.-negate params) "!" "")]
  #js{:values #js[(str "%" value "%")]
      :text   (str "SELECT id"
                   " FROM games"
                   " WHERE " field " " prefix "~~* {{}}")}))

(defn junction [schema params]
  (let [table  (.-table schema)
        field  (.-field schema)
        value  (.-value params)
        negate (boolean (.-negate params))]
    #js{:values [(str "%" value "%")]
        :text   (str "SELECT a.id
                      FROM games a, games_" table " ab, " table " b
                      WHERE a.id = ab.game_id
                        AND ab." field "_id = b.id
                      GROUP BY a.id
                      HAVING BOOL_OR(" field " ~~* {{}}) != " negate)}))

(defn- create-generator [s]
  (let [remaining (atom s)]
    (fn []
      (let [f (first @remaining)]
        (swap! remaining rest)
        f))))

(def ^:private parameter-templates (map #(str "$" (inc %)) (range)))

(defn- to-sql
  ([ast] (to-sql ast true))
  ([ast intersect]
   (.reduce ast
     (fn [acc cur]
       (let [joining-term (if intersect "INTERSECT ALL" "UNION ALL")
             is-or        (= "OR" (.-type cur))
             sql          (if is-or
                            (to-sql (.-terms cur) false)
                            ((g/get tl (.-tag cur)) cur))]
         #js{:values (.concat (.-values acc) (or (.-values sql) #js[]))
             :text (as-> sql $
                         (.-text $)
                         (if is-or
                           (str "(" $ ")")
                           $)
                         (if (empty? (.-text acc))
                           $
                           (str (.-text acc) " " joining-term " " $)))}))
     #js{:text "" :values #js[]})))

(defn transpile [query order direction offset]
  {:pre [(some (partial = order)
               (.-FIELDS tl))
         (#{"ASC" "DESC"} direction)]}
  (let [ast                   (.tryParse lang query)
        {:strs [text values]} (js->clj (to-sql ast))]
    (clj->js {:values (conj values offset)
              :text   (as-> text $
                            (if (empty? $)
                              "games"
                              (str "(" $ ") AS GameSubquery NATURAL INNER JOIN games"))
                            (str "SELECT DISTINCT " (.-CONCATENATED_FIELDS tl)
                                 " FROM " $
                                 " ORDER BY " order " " direction
                                 " LIMIT 25 OFFSET {{}}")
                            (s/replace $ #"\{\{\}\}" (create-generator parameter-templates)))})))

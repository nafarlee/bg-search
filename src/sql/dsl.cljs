(ns sql.dsl)

(defn clj->sql [& tokens]
  (letfn [(map-token [token]
            (cond
              ((some-fn keyword? string?) token)
              {:text [(name token)]
               :values []}

              (and (contains? token :text) (contains? token :values))
              token

              (nil? token)
              {:text []
               :values []}

              (set? token)
              {:text [:?]
               :values [(first token)]}

              (list? token)
              (update (reduce-tokens token)
                      :text
                      #(vec (concat "(" % ")")))

              (vector? token)
              {:values []
               :text (conj (mapv (comp #(str % ",") name)
                                 (butlast token))
                           (name (last token)))}))
          (reduce-tokens [tokens]
            (reduce #(merge-with (comp vec concat) %1 (map-token %2))
                             {:text [] :values []}
                             tokens))]
    (reduce-tokens tokens)))

(defn realize-query [{:keys [text values]}]
  (let [parameter-index (atom 1)]
    {:values values
     :text (->> text
                (mapv #(if (not= :? %)
                         %
                         (do
                           (swap! parameter-index inc)
                           (str "$" (dec @parameter-index)))))
                (s/join " "))}))

(ns language
  (:refer-clojure :exclude [seq string])
  (:require
    [goog.object :as g]
    ["parsimmon" :as ps]
    ["/language/tokens" :default tokens]))

(def alt (.-alt ps))
(def seq (.-seq ps))
(def end (.-end ps))
(def whitespace (.-whitespace ps))
(def opt-whitespace (.-optWhitespace ps))
(def regexp (.-regexp ps))
(def string (.-string ps))

(def language
   (.createLanguage
    ps
    #js{:Language
        #(alt (-> (seq opt-whitespace
                       (.-ExpressionList %)
                       opt-whitespace
                       end)
                  (.map second))
              (-> (seq whitespace end)
                  (.result #js[]))
              (.result end #js[]))
        
        :ExpressionList
        #(alt (-> (seq (.-Expression %)
                       whitespace
                       (.-ExpressionList %))
                  (.map (fn [exp _ exps] (.concat #js[exp] exps))))
              (-> (.-Expression %)
                  (.map array)))

        :Expression
        #(alt (.-OrChain %)
              (.-SubExpression %))

        :OrChain
        #(-> (seq (.-SubExpression %)
                  whitespace
                  (.-Or %)
                  whitespace
                  (.-SubExpression %)
                  (.many (seq whitespace
                              (.-Or %)
                              whitespace
                              (.-SubExpression %))))
             (.map (fn [[f _ _ _ s r]]
                     #js{:type  "OR"
                         :terms (.concat #js[f s] (.map r (fn [_ _ _ term] term)))})))

        :SubExpression
        #(alt (.-Group %)
              (.-Term %))

        :Group
        #(-> (seq (string "(")
                  opt-whitespace
                  (.-ExpressionList %)
                  opt-whitespace
                  (string ")"))
             (.map (fn [[_ _ terms]]
                     (if (= 1 (.-length terms))
                       (first terms)
                       #js{:type "AND" :terms terms}))))

        :Term
        #(-> (seq (-> (string "-") (.atMost 1))
                  (alt (.-DeclarativeTerm %)
                       (.-RelationalTerm %)
                       (.-MetaTerm %)))
             (.map (fn [parsed]
                     (let [sign (ffirst parsed)
                           term (second parsed)]
                       (js/Object.assign #js{:negate (= sign "-")} term)))))

        :MetaTerm
        #(-> (seq (regexp #"(?i)is:")
                  (.-MetaTag %))
             (.map (fn [parsed]
                     (let [value (second parsed)]
                       #js{:type "META"
                           :tag  (g/get (.. tokens -tags -meta) value)}))))

        :MetaTag
        #(regexp (js/RegExp. (-> (.. tokens -tags -meta)
                                 js/Object.keys
                                 (.join "|"))
                             "i"))

        :DeclarativeTerm
        #(-> (seq (.-DeclarativeTag %)
                  (string ":")
                  (.-Value %))
             (.map (fn [parsed]
                     (let [tag       (first parsed)
                           value     (nth parsed 2)]
                       #js{:type  "DECLARATIVE"
                           :value value
                           :tag   (g/get (.. tokens -tags -declarative) (.toLowerCase tag))}))))

        :RelationalTerm
        #(-> (seq (.-RelationalTag %)
                  (.-RelationalOperator %)
                  (.-SimpleValue %))
             (.map (fn [[tag operator value]]
                     #js{:type "RELATIONAL"
                         :tag (g/get (.. tokens -tags -relational) (.toLowerCase tag))
                         :operator (g/get (.-operators tokens) operator)
                         :value value})))

        :Value
        #(alt (.-SimpleValue %)
              (.-QuotedValue %))
        
        :Or
        #(regexp #"(?i)or")

        :DeclarativeTag
        #(regexp (js/RegExp. (-> (.. tokens -tags -declarative)
                              js/Object.keys
                              (.join "|"))
                          "i"))

        :SimpleValue
        #(regexp #"[^\"][^) ]*")

        :QuotedValue
        #(regexp #"\"([^\"]+)\"" 1)

        :RelationalTag
        #(regexp (js/RegExp. (-> (.. tokens -tags -relational) js/Object.keys (.join "|")) "i"))

        :RelationalOperator
        #(regexp (js/RegExp. (-> (.-operators tokens)
                              js/Object.keys
                              (.sort (fn [a b] (- (.-length b) (.-length a))))
                              (.join "|"))))}))

(ns language
  (:refer-clojure :exclude [seq string])
  (:require
    [clojure.string :refer [join]]
    ["parsimmon" :as ps]))

(def ^:private operators
  #{"="
    ">"
    ">="
    "<"
    "<="})

(def ^:private meta-tags
  {"e"                "EXPANSION"
   "expansion"        "EXPANSION"
   "c"                "COLLECTION"
   "collection"       "COLLECTION"
   "reimplementation" "REIMPLEMENTATION"
   "r"                "REIMPLEMENTATION"})

(def ^:private declarative-tags
  {"name"     "NAME"
   "n"        "NAME"
   "art"      "ARTIST"
   "a"        "ARTIST"
   "category" "CATEGORY"
   "c"        "CATEGORY"
   "desc"     "DESCRIPTION"
   "family"   "FAMILY"
   "f"        "FAMILY"
   "mechanic" "MECHANIC"
   "m"        "MECHANIC"
   "publish"  "PUBLISHER"
   "p"        "PUBLISHER"
   "design"   "DESIGNER"
   "desi"     "DESIGNER"})

(def ^:private relational-tags
  {"rating-votes"      "RATING_VOTES"
   "rv"                "RATING_VOTES"
   "average-rating"    "AVERAGE_RATING"
   "ar"                "AVERAGE_RATING"
   "geek-rating"       "GEEK_RATING"
   "gr"                "GEEK_RATING"
   "steamdb-rating"    "STEAMDB_RATING"
   "sr"                "STEAMDB_RATING"
   "rating-deviation"  "RATING_DEVIATION"
   "rd"                "RATING_DEVIATION"
   "average-weight"    "AVERAGE_WEIGHT"
   "aw"                "AVERAGE_WEIGHT"
   "weight-votes"      "WEIGHT_VOTES"
   "wv"                "WEIGHT_VOTES"
   "year"              "YEAR"
   "age"               "AGE"
   "rec-players"       "RECOMMENDED_PLAYERS"
   "rp"                "RECOMMENDED_PLAYERS"
   "best-players"      "BEST_PLAYERS"
   "bp"                "BEST_PLAYERS"
   "quorum-players"    "QUORUM_PLAYERS"
   "qp"                "QUORUM_PLAYERS"
   "min-players"       "MINIMUM_PLAYERS"
   "mnpr"              "MINIMUM_PLAYERS"
   "max-players"       "MAXIMUM_PLAYERS"
   "mxpr"              "MAXIMUM_PLAYERS"
   "min-playtime"      "MINIMUM_PLAYTIME"
   "mnpt"              "MINIMUM_PLAYTIME"
   "max-playtime"      "MAXIMUM_PLAYTIME"
   "mxpt"              "MAXIMUM_PLAYTIME"
   "median-playtime-1" "MEDIAN_PLAYTIME_1"
   "mdpt1"             "MEDIAN_PLAYTIME_1"
   "median-playtime-2" "MEDIAN_PLAYTIME_2"
   "mdpt2"             "MEDIAN_PLAYTIME_2"
   "median-playtime-3" "MEDIAN_PLAYTIME_3"
   "mdpt3"             "MEDIAN_PLAYTIME_3"
   "median-playtime-4" "MEDIAN_PLAYTIME_4"
   "mdpt4"             "MEDIAN_PLAYTIME_4"
   "median-playtime-5" "MEDIAN_PLAYTIME_5"
   "mdpt5"             "MEDIAN_PLAYTIME_5"
   "median-playtime"   "MEDIAN_PLAYTIME"
   "mdpt"              "MEDIAN_PLAYTIME"})

(def alt (.-alt ps))
(def seq (.-seq ps))
(def end (.-end ps))
(def whitespace (.-whitespace ps))
(def opt-whitespace (.-optWhitespace ps))
(def regexp (.-regexp ps))
(def string (.-string ps))

(def language
  (.-Language
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
                  (.map (fn [[exp _ exps]] (.concat #js[exp] exps))))
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
                           :tag  (meta-tags value)}))))

        :MetaTag
        #(apply alt (map string (keys meta-tags)))

        :DeclarativeTerm
        #(-> (seq (.-DeclarativeTag %)
                  (string ":")
                  (.-Value %))
             (.map (fn [parsed]
                     (let [tag       (first parsed)
                           value     (nth parsed 2)]
                       #js{:type  "DECLARATIVE"
                           :value value
                           :tag   (declarative-tags (.toLowerCase tag))}))))

        :RelationalTerm
        #(-> (seq (.-RelationalTag %)
                  (.-RelationalOperator %)
                  (.-SimpleValue %))
             (.map (fn [[tag operator value]]
                     #js{:type "RELATIONAL"
                         :tag (relational-tags (.toLowerCase tag))
                         :operator (operators operator)
                         :value value})))

        :Value
        #(alt (.-SimpleValue %)
              (.-QuotedValue %))
        
        :Or
        #(regexp #"(?i)or")

        :DeclarativeTag
        #(apply alt (map string (keys declarative-tags)))

        :SimpleValue
        #(regexp #"[^\"][^) ]*")

        :QuotedValue
        #(regexp #"\"([^\"]+)\"" 1)

        :RelationalTag
        #(apply alt (map string (keys relational-tags)))

        :RelationalOperator
        #(apply alt (map string operators))})))

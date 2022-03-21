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
  {"e"                      "EXPANSION"
   "expansion"              "EXPANSION"
   "pce"                    "PLAYER_COUNT_EXPANSION"
   "player-count-expansion" "PLAYER_COUNT_EXPANSION"
   "c"                      "COLLECTION"
   "collection"             "COLLECTION"
   "reimplementation"       "REIMPLEMENTATION"
   "r"                      "REIMPLEMENTATION"})

(def ^:private declarative-tags
  {"name"     "NAME"
   "n"        "NAME"
   "own"      "OWN"
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
  {"rating-votes"        "RATING_VOTES"
   "rv"                  "RATING_VOTES"
   "language-dependence" "LANGUAGE_DEPENDENCE"
   "ld"                  "LANGUAGE_DEPENDENCE"
   "average-rating"      "AVERAGE_RATING"
   "ar"                  "AVERAGE_RATING"
   "geek-rating"         "GEEK_RATING"
   "gr"                  "GEEK_RATING"
   "steamdb-rating"      "STEAMDB_RATING"
   "sr"                  "STEAMDB_RATING"
   "rating-deviation"    "RATING_DEVIATION"
   "rd"                  "RATING_DEVIATION"
   "average-weight"      "AVERAGE_WEIGHT"
   "aw"                  "AVERAGE_WEIGHT"
   "weight-votes"        "WEIGHT_VOTES"
   "wv"                  "WEIGHT_VOTES"
   "year"                "YEAR"
   "age"                 "AGE"
   "rec-players"         "RECOMMENDED_PLAYERS"
   "rp"                  "RECOMMENDED_PLAYERS"
   "best-players"        "BEST_PLAYERS"
   "bp"                  "BEST_PLAYERS"
   "quorum-players"      "QUORUM_PLAYERS"
   "qp"                  "QUORUM_PLAYERS"
   "min-players"         "MINIMUM_PLAYERS"
   "mnpr"                "MINIMUM_PLAYERS"
   "max-players"         "MAXIMUM_PLAYERS"
   "mxpr"                "MAXIMUM_PLAYERS"
   "min-playtime"        "MINIMUM_PLAYTIME"
   "mnpt"                "MINIMUM_PLAYTIME"
   "max-playtime"        "MAXIMUM_PLAYTIME"
   "mxpt"                "MAXIMUM_PLAYTIME"
   "median-playtime-1"   "MEDIAN_PLAYTIME_1"
   "mdpt1"               "MEDIAN_PLAYTIME_1"
   "median-playtime-2"   "MEDIAN_PLAYTIME_2"
   "mdpt2"               "MEDIAN_PLAYTIME_2"
   "median-playtime-3"   "MEDIAN_PLAYTIME_3"
   "mdpt3"               "MEDIAN_PLAYTIME_3"
   "median-playtime-4"   "MEDIAN_PLAYTIME_4"
   "mdpt4"               "MEDIAN_PLAYTIME_4"
   "median-playtime-5"   "MEDIAN_PLAYTIME_5"
   "mdpt5"               "MEDIAN_PLAYTIME_5"
   "median-playtime"     "MEDIAN_PLAYTIME"
   "mdpt"                "MEDIAN_PLAYTIME"})

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
                       ^js(.-ExpressionList %)
                       opt-whitespace
                       end)
                  (.map second))
              (-> (seq whitespace end)
                  (.result #js[]))
              (.result end #js[]))
        
        :ExpressionList
        #(alt (-> (seq ^js(.-Expression %)
                       whitespace
                       ^js(.-ExpressionList %))
                  (.map (fn [[exp _ exps]] (.concat #js[exp] exps))))
              (-> ^js(.-Expression %)
                  (.map array)))

        :Expression
        #(alt ^js(.-OrChain %)
              ^js(.-SubExpression %))

        :OrChain
        #(-> (seq ^js(.-SubExpression %)
                  whitespace
                  ^js(.-Or %)
                  whitespace
                  ^js(.-SubExpression %)
                  ^js(.many (seq whitespace
                                 ^js(.-Or %)
                                 whitespace
                                 ^js(.-SubExpression %))))
             (.map (fn [[f _ _ _ s r]]
                     #js{:type  "OR"
                         :terms (.concat #js[f s]
                                         (.map r (fn [[_ _ _ term]] term)))})))

        :SubExpression
        #(alt ^js(.-Group %)
              ^js(.-Term %))

        :Group
        #(-> (seq (string "(")
                  opt-whitespace
                  ^js(.-ExpressionList %)
                  opt-whitespace
                  (string ")"))
             (.map (fn [[_ _ terms]]
                     (if (= 1 (.-length terms))
                       (first terms)
                       #js{:type "AND" :terms terms}))))

        :Term
        #(-> (seq ^js(.atMost (string "-") 1)
                  (alt ^js(.-DeclarativeTerm %)
                       ^js(.-RelationalTerm %)
                       ^js(.-MetaTerm %)))
             (.map (fn [parsed]
                     (let [sign (ffirst parsed)
                           term (second parsed)]
                       (js/Object.assign #js{:negate (= sign "-")} term)))))

        :MetaTerm
        #(-> (seq (regexp #"(?i)is:")
                  ^js(.-MetaTag %))
             (.map (fn [parsed]
                     (let [value (second parsed)]
                       #js{:type "META"
                           :tag  (meta-tags value)}))))

        :MetaTag
        #(apply alt (map string (sort-by (comp - count) (keys meta-tags))))

        :DeclarativeTerm
        #(-> (seq ^js(.-DeclarativeTag %)
                  (string ":")
                  ^js(.-Value %))
             (.map (fn [parsed]
                     (let [tag       (first parsed)
                           value     (nth parsed 2)]
                       #js{:type  "DECLARATIVE"
                           :value value
                           :tag   (declarative-tags (.toLowerCase tag))}))))

        :RelationalTerm
        #(-> (seq ^js(.-RelationalTag %)
                  ^js(.-RelationalOperator %)
                  ^js(.-SimpleValue %))
             (.map (fn [[tag operator value]]
                     #js{:type "RELATIONAL"
                         :tag (relational-tags (.toLowerCase tag))
                         :operator (operators operator)
                         :value value})))

        :Value
        #(alt ^js(.-SimpleValue %)
              ^js(.-QuotedValue %))
        
        :Or
        #(regexp #"(?i)or")

        :DeclarativeTag
        #(apply alt (map string (sort-by (comp - count) (keys declarative-tags))))

        :SimpleValue
        #(regexp #"[^\"][^) ]*")

        :QuotedValue
        #(regexp #"\"([^\"]+)\"" 1)

        :RelationalTag
        #(->> relational-tags
              keys
              (sort-by (comp - count))
              (map string)
              (apply alt))

        :RelationalOperator
        #(apply alt (map string operators))})))

(ns transpile
  (:require
    sql
    [clojure.string :as s]
    ["/language/index" :default lang]))

(defn simple [field {:strs [value negate]}]
  (sql/clj->sql :select :id
                :from :games
                :where field (if negate "!~~*" "~~*") #{value}))

(defn junction [{:keys [table field]} {:strs [value negate]}]
  (sql/clj->sql :select :a.id
                :from ["games a" (str "games_" table " ab") (str table " b")]
                :where :a.id := :ab.game_id
                  :and (str "ab." field "_id") := :b.id
                :group :by :a.id
                :having :bool_or (list field "~~*" #{(str "%" value "%")}) :!= (-> negate boolean str)))

(defn relational [field {:strs [value operator negate]}]
  (sql/clj->sql :select :id
                :from :games
                :where (when negate :not) field operator #{value}))

(defn ->range [op x]
  (case op
    ">" (str "(" x ",)")
    "<" (str "(," x ")")
    "<=" (str "(," x "]")
    ">=" (str "[" x ",)")
    "=" (str "[" x "," x "]")
    nil))

(defn recommendation [clause {:strs [operator value negate]}]
  (sql/clj->sql :select :a.id
                :from ["games a" "player_recommendations b"]
                :where :a.id := :b.id
                  :and :players "&&" #{(->range operator value)} "::int4range"
                  :and (when negate :not) clause))

(defn self-junction [{:keys [table join-field nullable-field]} {:strs [negate]}]
  (sql/clj->sql :select :id
                :from :games
                :left :join table
                  :on :id := join-field
                :where nullable-field :is (when-not negate :not) :null))

(defn median-playtime [{:strs [operator value negate]}]
  (sql/clj->sql :select :game_id :as :id
                :from :play_medians
                :where :players := :0
                  :and (when negate :not) :median operator #{value}))

(def exported-fields
  ["id"
   "primary_name"
   "rating_votes"
   "average_rating"
   "steamdb_rating"
   "bayes_rating"
   "rating_deviation"
   "average_weight"
   "weight_votes"
   "year"
   "minimum_age"
   "minimum_players"
   "maximum_players"
   "minimum_playtime"
   "maximum_playtime"
   "description"])

(def terms
  (reduce
    (fn [m [k v]]
      (assoc m
             (-> k name s/upper-case (s/replace "-" "_"))
             v))
    {}
    {:name                (partial simple :primary_name)
     :description         (partial simple :description)
     :artist              (partial junction {:table "artists" :field "artist"})
     :category            (partial junction {:table "categories" :field "category"})
     :family              (partial junction {:table "families" :field "family"})
     :mechanic            (partial junction {:table "mechanics" :field "mechanic"})
     :publisher           (partial junction {:table "publishers" :field "publisher"})
     :designer            (partial junction {:table "designers" :field "designer"})
     :rating-votes        (partial relational :rating_votes)
     :average-rating      (partial relational :average_rating)
     :geek-rating         (partial relational :bayes_rating)
     :steamdb-rating      (partial relational :steamdb_rating)
     :rating-deviation    (partial relational :rating_deviation)
     :average-weight      (partial relational :average_weight)
     :weight-votes        (partial relational :weight_votes)
     :year                (partial relational :year)
     :age                 (partial relational :minimum_age)
     :minimum-players     (partial relational :minimum_players)
     :maximum-players     (partial relational :maximum_players)
     :minimum-playtime    (partial relational :minimum_playtime)
     :maximum-playtime    (partial relational :maximum_playtime)
     :recommended-players (partial recommendation
                                   (sql/clj->sql :recommended :> (list :best :+ :not_recommended)))
     :best-players        (partial recommendation
                                   (sql/clj->sql :best :> (list :recommended :+ :not_recommended)))
     :quorum-players      (partial recommendation
                                   (sql/clj->sql (list :best :+ :recommended)
                                                 :>=
                                                 (list :not_recommended "/" :3.0 :* :7.0)))
     :median-playtime     median-playtime
     :reimplementation    (partial self-junction {:table "reimplementations"
                                                  :join-field "reimplementation"
                                                  :nullable-field "original"})
     :expansion           (partial self-junction {:table "expansions"
                                                  :join-field "expansion"
                                                  :nullable-field "base"})
     :collection          (partial self-junction {:table "collections"
                                                  :join-field "collection"
                                                  :nullable-field "item"})}))

(defn- to-sql
  ([ast]
   (to-sql ast true))
  ([ast intersect]
   (->> ast
        (map (fn [{:strs [tag type] :as term}]
               (if (not= type "OR")
                 ((get terms tag) term)
                 (-> (get term "terms")
                     (to-sql false)
                     (update :text #(concat ["("] % [")"]))))))
        (reduce (fn [acc cur]
                  {:values (concat (:values acc) (:values cur))
                   :text (concat (:text acc)
                                 (if intersect ["intersect" "all"] ["union" "all"])
                                 (:text cur))})))))

(defn transpile [query order direction offset]
  {:pre [(some (partial = order) exported-fields)
         (#{"ASC" "DESC"} direction)]}
  (if (empty? query)
    (sql/clj->sql :select :distinct exported-fields
                  :from :games
                  :order :by order direction
                  :limit :25 :offset #{offset})
    (sql/clj->sql :select :distinct exported-fields
                  :from (list (to-sql (js->clj (.tryParse lang query)))) :as :GameSubquery
                    :natural :inner :join :games
                  :order :by order direction
                  :limit :25 :offset #{offset})))

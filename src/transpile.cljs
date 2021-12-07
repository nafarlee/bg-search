(ns transpile
  (:require
    [sql.dsl :refer [clj->sql]]
    [clojure.string :as s]
    [language :refer [language] :rename {language lang}]))

(defn simple [field {:strs [value negate]}]
  (clj->sql :select :id
            :from :games
            :where field (when negate :not) :ilike #{(str "%" value "%")}))

(defn junction [{:keys [table field]} {:strs [value negate]}]
  (clj->sql :select :ab.game_id :as :id
            :from (str "games_" table) :ab
            :inner :join
              table :b
              :on (str "ab." field "_id") := :b.id
            :group :by :ab.game_id
            :having
              :bool_or (list (str "b." field) :ilike #{(str "%" value "%")})
              :=
              (if negate :FALSE :TRUE)))

(defn relational [field {:strs [value operator negate]}]
  (clj->sql :select :id
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
  (clj->sql :select :id
            :from :player_recommendations
            :where :players "&&" #{(->range operator value)} "::int4range"
              :and (when negate :not) clause))

(defn self-junction [{:keys [table join-field nullable-field]} {:strs [negate]}]
  (clj->sql :select :id
                :from :games
                :left :join table
                  :on :id := join-field
                :where nullable-field :is (when-not negate :not) :null))

(defn median-playtime [player-count {:strs [operator value negate]}]
  (clj->sql :select :game_id :as :id
                :from :play_medians
                :where :players := (str player-count)
                  :and (when negate :not) :median operator #{value}))

(defn own [{:strs [value negate]}]
  (clj->sql
   :select :id
   :from :games
   :left :join :player_collections
     :on :id := :game_id
   :where
     (if negate
       (list :username :!= #{value} :or :not :own)
       (list :username := #{value} :and :own))))

(defn player-count-expansion [{:strs [negate]}]
  (clj->sql :select :e.id :as :id
            :from :expansions :mid
              :inner :join :games :b :on :mid.base := :b.id
              :inner :join :games :e :on :mid.expansion := :e.id
            :where (when negate :not) :e.maximum_players :> :b.maximum_players))

(def exported-fields
  ["id"
   "primary_name"
   "thumbnail"
   "year"])

(def orderable-fields
  ["id"
   "primary_name"
   "thumbnail"
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
                                   (clj->sql :recommended :> (list :best :+ :not_recommended)))
     :best-players        (partial recommendation
                                   (clj->sql :best :> (list :recommended :+ :not_recommended)))
     :quorum-players      (partial recommendation :is_quorum)
     :median-playtime     (partial median-playtime 0)
     :median-playtime-1   (partial median-playtime 1)
     :median-playtime-2   (partial median-playtime 2)
     :median-playtime-3   (partial median-playtime 3)
     :median-playtime-4   (partial median-playtime 4)
     :median-playtime-5   (partial median-playtime 5)
     :own                 own
     :player-count-expansion player-count-expansion
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
               (case type
                 "OR"
                 (-> (get term "terms")
                     (to-sql false)
                     (update :text #(concat ["("] % [")"])))

                 "AND"
                 (-> (get term "terms")
                     (to-sql true)
                     (update :text #(concat ["("] % [")"])))

                 ((get terms tag) term))))
        (reduce (fn [acc cur]
                  {:values (concat (:values acc) (:values cur))
                   :text (concat (:text acc)
                                 (if intersect ["intersect" "all"] ["union" "all"])
                                 (:text cur))})))))

(defn transpile [query order direction offset]
  {:pre [(some (partial = order) orderable-fields)
         (#{"ASC" "DESC"} direction)]}
  (if (empty? query)
    (clj->sql :select :distinct (conj exported-fields order)
              :from :games
              :where order :is :not :null
              :order :by order direction
              :limit :25 :offset #{offset})
    (clj->sql :select :distinct (conj exported-fields order)
              :from (list (to-sql (js->clj (.tryParse lang query))))
                :as :GameSubquery
              :natural :inner :join :games
              :where order :is :not :null
              :order :by order direction
              :limit :25 :offset #{offset})))

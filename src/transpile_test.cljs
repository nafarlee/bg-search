(ns transpile-test
  (:require
    [sql.dsl :refer [clj->sql]]
    [clojure.string :as s]
    [transpile :as t]
    [constants :refer [results-per-page]]
    [cljs.test :refer [deftest is]]))

(defn compact-whitespace [s]
  (s/replace s #"\s+" " "))

(deftest transpile
  (is (= (t/transpile "n:scythe" "id" "DESC" 0)
          (clj->sql :select :distinct (vec t/exported-fields)
                        :from (list :select :id
                                    :from :games
                                    :where :primary_name "~~*" #{"%scythe%"}) :as :GameSubquery
                          :natural :inner :join :games
                        :order :by :id :DESC
                        :limit results-per-page :offset #{0})))
  (is (= (t/transpile "" "id" "DESC" 0)
         (clj->sql :select :distinct (vec t/exported-fields)
                       :from :games
                       :order :by :id :DESC
                       :limit results-per-page :offset #{0}))))

(deftest simple
  (is (= (t/simple :fruit {"value" "pear"})
         (clj->sql :select :id
                       :from :games
                       :where :fruit "~~*" #{"%pear%"}))))

(deftest junction
  (is (= (t/junction {:table "recipes" :field "fruit"} {"value" "pear"})
         (clj->sql :select :a.id
                       :from ["games a" "games_recipes ab" "recipes b"]
                       :where :a.id := :ab.game_id
                         :and :ab.fruit_id := :b.id
                       :group :by :a.id
                       :having :bool_or (list :fruit "~~*" #{"%pear%"}) :!= :false))))

(deftest relational
  (is (= (t/relational "fruit" {"value" "pear" "operator" "="})
         (clj->sql :select :id
                       :from :games
                       :where :fruit := #{"pear"}))))

(deftest self-junction
  (is (= (t/self-junction {:table "table" :join-field "child" :nullable-field "parent"} {})
          (clj->sql :select :id
                        :from :games
                        :left :join :table
                          :on :id := :child
                        :where :parent :is :not :null))))

(deftest recommendation
  (is (= (t/recommendation {:text   ["recommended" ">" "(" "best" "+" "not_recommended" ")"]
                            :values []}
                           {"operator" "=" "value" 42})
         (clj->sql :select :a.id
                       :from ["games a" "player_recommendations b"]
                       :where :a.id := :b.id
                         :and :players "&&" #{"[42,42]"} "::int4range"
                         :and :recommended :> (list :best :+ :not_recommended)))))

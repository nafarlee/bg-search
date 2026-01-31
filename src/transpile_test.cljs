(ns transpile-test
  (:require
    [sql.dsl :refer [clj->sql]]
    [transpile :as t]
    [constants :refer [results-per-page]]
    [cljs.test :refer [deftest is]]))

(deftest transpile
  (is (= (t/transpile "n:scythe" "id" "DESC" 0 "10")
          (clj->sql :select :distinct (vec t/exported-fields)
                        :from (list :select :id
                                    :from :games
                                    :where :primary_name :ilike #{"%scythe%"}) :as :GameSubquery
                          :natural :inner :join :games
                        :where :id :is :not :null
                        :order :by :id :DESC
                        :limit (str results-per-page) :offset #{0})))
  (is (= (t/transpile "" "id" "DESC" 0 "10")
         (clj->sql :select :distinct (vec t/exported-fields)
                       :from :games
                       :where :id :is :not :null
                       :order :by :id :DESC
                       :limit (str results-per-page) :offset #{0}))))

(deftest simple
  (is (= (t/simple :fruit {"value" "pear"})
         (clj->sql :select :id
                       :from :games
                       :where :fruit :ilike #{"%pear%"}))))

(deftest junction
  (is (= (t/junction {:table "recipes" :field "fruit"} {"value" "pear"})
         (clj->sql :select :ab.game_id :as :id
                   :from :games_recipes :ab
                     :inner :join :recipes :b :on :ab.fruit_id := :b.id
                   :group :by :ab.game_id
                   :having :bool_or (list :b.fruit :ilike #{"%pear%"}) := :TRUE))))

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
         (clj->sql :select :id
                   :from :player_recommendations
                   :where :players "&&" #{"[42,42]"} "::int4range"
                     :and :recommended :> (list :best :+ :not_recommended)))))

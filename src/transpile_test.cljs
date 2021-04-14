(ns transpile-test
  (:require
    sql
    [clojure.string :as s]
    [transpile :as t]
    [cljs.test :refer [deftest is]]))

(defn compact-whitespace [s]
  (s/replace s #"\s+" " "))

(deftest transpile
  (is (= (t/transpile "" "id" "DESC" 0)
         (-> (sql/clj->sql :select :distinct t/exported-fields
                           :from :games
                           :order :by :id :desc
                           :limit :25 :offset #{0})
             sql/realize-query
             clj->js))))

(deftest simple
  (is (= (t/simple :fruit {:value "pear"})
         (sql/clj->sql :select :id
                       :from :games
                       :where :fruit "~~*" #{"pear"}))))

(deftest junction
  (is (= (t/junction {:table "recipes" :field "fruit"} {:value "pear"})
         (sql/clj->sql :select :a.id
                       :from ["games a" "games_recipes ab" "recipes b"]
                       :where :a.id := :ab.game_id
                         :and :ab.fruit_id := :b.id
                       :group :by :a.id
                       :having :bool_or (list :fruit "~~*" #{"%pear%"}) :!= :false))))

(deftest relational
  (is (= (t/relational "fruit" {:value "pear" :operator "="})
         (sql/clj->sql :select :id
                       :from :games
                       :where :fruit := #{"pear"}))))

(deftest self-junction
  (is (= (t/self-junction {:table "table" :join-field "child" :nullable-field "parent"} {})
          (sql/clj->sql :select :id
                        :from :games
                        :left :join :table
                          :on :id := :child
                        :where :parent :is :not :null))))

(deftest recommendation
  (is (= (t/recommendation {:text   ["recommended" ">" "(" "best" "+" "not_recommended" ")"]
                            :values []}
                           {:operator "=" :value 42})
         (sql/clj->sql :select :a.id
                       :from ["games a" "player_recommendations b"]
                       :where :a.id := :b.id
                         :and :players "&&" #{"[42,42]"} "::int4range"
                         :and :recommended :> (list :best :+ :not_recommended)))))

(ns sql-test 
  (:require
    [sql]
    [cljs.test :refer [deftest is]]))

(deftest clj->sql
  (is (= {:text "select id from table" :values []}
         (sql/clj->sql :select :id
                       :from :table)))
  (is (= {:text "select id, name from table" :values []}
         (sql/clj->sql :select [:id :name]
                       :from :table)))
  (is (= {:text "select true and ( true or false )" :values []}
         (sql/clj->sql :select :true :and '(:true :or :false)))))

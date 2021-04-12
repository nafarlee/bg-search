(ns sql-test 
  (:require
    [sql]
    [cljs.test :refer [deftest is]]))

(deftest clj->sql
  (is (= {:text "select id from table" :values []}
         (sql/clj->sql :select :id
                       :from :table))))

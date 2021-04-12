(ns sql-test 
  (:require
    [sql]
    [cljs.test :refer [deftest is]]))

(deftest clj->sql
  (is (= {:text ["select" "id" "from" "table" "where" "id" "=" :? "or" "id" "=" :?] :values [1 2]}
         (sql/clj->sql :select :id
                       :from :table
                       :where :id := #{1}
                          :or :id := #{2})))
  (is (= {:text ["select" "id" "from" "table"] :values []}
         (sql/clj->sql :select :id
                       :from :table)))
  (is (= {:text ["select" "id," "name" "from" "table"] :values []}
         (sql/clj->sql :select [:id :name]
                       :from :table)))
  (is (= {:text ["select" "true" "and" "(" "true" "or" "false" ")"] :values []}
         (sql/clj->sql :select :true :and '(:true :or :false)))))

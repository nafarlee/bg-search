(ns sql-test
  (:require
    [sql.dsl :refer [clj->sql realize-query]]
    [cljs.test :refer [deftest is]]))

(deftest clj->sql
  (is (= {:text ["select" "id" "from" "table" "where" "id" "=" :? "or" "id" "=" :?] :values [1 2]}
         (clj->sql :select :id
                       :from :table
                       :where :id := #{1}
                          :or :id := #{2})))
  (is (= {:text ["select" "id" "from" "table"] :values []}
         (clj->sql :select :id
                       :from :table)))
  (is (= {:text ["select" "id," "name" "from" "table"] :values []}
         (clj->sql :select [:id :name]
                       :from :table)))
  (is (= {:text ["select" "true" "and" "(" "true" "or" "false" ")"] :values []}
         (clj->sql :select :true :and '(:true :or :false)))))

(deftest realize-query
  (is (= {:text "select id from table where id = $1"
          :values [1]}
         (realize-query (clj->sql :select :id :from :table :where :id := #{1})))))

(ns sql-test
  (:require
    [sql]
    ["/db/insert" :refer [toSQL]]
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

(deftest realize-query
  (is (= {:text "select id from table where id = $1"
          :values [1]}
         (sql/realize-query (sql/clj->sql :select :id :from :table :where :id := #{1})))))

(deftest ->inserts
  (is (= (sql/generate-insert "my-table"
                              ["id" "name"]
                              ["id"]
                              [[1 "banana"]
                               [2 "pear"]])
         {:text ["insert" "into" "my-table" "(" "id," "name" ")"
                 "values" "(" :? "," :? ")" "," "(" :? "," :? ")"
                 "on" "conflict" "(" "id" ")" "do" "update" "set"
                 "id" "=" "EXCLUDED.id" ","
                 "name" "=" "EXCLUDED.name"],
          :values [1 "banana" 2 "pear"]})))

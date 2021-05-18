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
  (is (= (js->clj (toSQL "my-table"
                         #js["id" "name"]
                         #js["id"]
                         (clj->js [[1 "banana"]
                                   [2 "pear"]])))
         ["INSERT INTO my-table (id, name)\n     VALUES ($1, $2), ($3, $4)\n     ON CONFLICT (id)\n     DO UPDATE SET id = EXCLUDED.id, name = EXCLUDED.name;"
          [1 "banana" 2 "pear"]])))

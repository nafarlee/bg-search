(ns sql.insert-test
  (:require
    [sql.insert :as insert]
    [cljs.test :refer [deftest is]]))

(deftest generate
  (is (= (insert/generate "my-table"
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

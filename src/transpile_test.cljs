(ns transpile-test
  (:require
    ["/transpile/lib" :default tl]
    [clojure.string :as s]
    [transpile :as t]
    [cljs.test :refer [deftest is]]))

(defn compact-whitespace [s]
  (s/replace s #"\s+" " "))

(deftest transpile
  (let [actual (t/transpile "" "id" "DESC" 0)
        text   (s/replace (.-text actual) #"\s+" " ")
        values (js->clj (.-values actual))]
    (is (= text "SELECT DISTINCT id, primary_name, rating_votes, average_rating, steamdb_rating, bayes_rating, rating_deviation, average_weight, weight_votes, year, minimum_age, minimum_players, maximum_players, minimum_playtime, maximum_playtime, description FROM games ORDER BY id DESC LIMIT 25 OFFSET $1"))
    (is (= values [0]))))

(deftest simple
  (let [simple-fruit          (partial t/simple "fruit")
        value                 "pear"
        {:strs [text values]} (js->clj (simple-fruit #js{:value value}))]
    (is (= values [(str "%" value "%")]))
    (is (= (compact-whitespace text)
           "SELECT id FROM games WHERE fruit ~~* {{}}"))))

(deftest junction
  (let [schema                 #js{:table "recipes" :field "fruit"}
        junction-fruit-recipes (partial t/junction schema)
        params                 #js{:value "pear"}
        {:strs [text values]}  (js->clj (junction-fruit-recipes params))]
    (is (= values ["%pear%"]))
    (is (= (compact-whitespace text)
           "SELECT a.id FROM games a, games_recipes ab, recipes b WHERE a.id = ab.game_id AND ab.fruit_id = b.id GROUP BY a.id HAVING BOOL_OR(fruit ~~* {{}}) != false"))))

(deftest relational
  (let [relational-fruit      ((.-__relational tl) "fruit")
        params                #js{:value "pear" :operator "="}
        {:strs [text values]} (js->clj (relational-fruit params))]
    (is (= values ["pear"]))
    (is (= (compact-whitespace text)
           (compact-whitespace "SELECT id
                                FROM games
                                WHERE fruit = {{}}")))))

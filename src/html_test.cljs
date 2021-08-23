(ns html-test
  (:require
    html
    [cljs.test :refer [deftest is]]))

(deftest clj->html
  (is (= (html/clj->html [:h1 "Hello!"])
         "<h1>Hello!</h1>")))

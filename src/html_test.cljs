(ns html-test
  (:require
    html
    [cljs.test :refer [deftest is]]))

(deftest clj->html
  (is (= (html/clj->html [:h1 "Hello!"])
         "<h1>Hello!</h1>"))
  (is (= (html/clj->html [:h1 {:class "hidden"} "Hello!"])
         "<h1 class=\"hidden\">Hello!</h1>"))
  (is (= "<html><head><title>Hello</title></head><body><h1>World</h1></body></html>"
         (html/clj->html [:html
                          [:head
                           [:title "Hello"]]
                          [:body
                           [:h1 "World"]]]))))

(ns html-test
  (:require
    [html :as h]
    [cljs.test :refer [deftest is]]))

(deftest html
  (is (= (h/html [:h1 "Hello!"])
         "<h1>Hello!</h1>"))
  (is (= (h/html [:h1 {:class "hidden"} "Hello!"])
         "<h1 class=\"hidden\">Hello!</h1>"))
  (is (= "<html><head><title>Hello</title></head><body><h1>World</h1></body></html>"
         (h/html  [:html
                   [:head
                    [:title "Hello"]]
                   [:body
                    [:h1 "World"]]])))
  (is (= "<h1>A</h1><h1>B</h1>"
         (h/html (list
                  [:h1 "A"]
                  [:h1 "B"]))))
  (is (= "<ul><li>1</li><li>2</li></ul>"
         (h/html [:ul
                  [:li 1]
                  (when false [:li 1.5])
                  [:li 2]])))
  (is (= "<p class=\"hidden primary center\">Hello!</p>"
         (h/html [:p.hidden.primary {:class "center"}
                  "Hello!"]))))

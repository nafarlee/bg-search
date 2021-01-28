(ns core
  (:require
    ["/get" :default http-get]))

(defn main [& args]
  (-> "https://jsonplaceholder.typicode.com/todos/1"
      http-get
      (.then js/console.log)))

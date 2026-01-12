(ns pull-plays
 (:require
  [cljs.core.async :refer [go-loop]]
  [cljs.core.async.interop :refer-macros [<p!]]
  [util :refer [with-retry]]
  [http :refer [sleep]]
  [sql :refer [pool]]))

(defn pull-plays [db]
  (go-loop []
    (js/console.log (<p! (.query db "SELECT 42")))
    (<p! (sleep 5000))
    (recur)))

(defn main []
  (let [db (pool)]
    (.then (with-retry #(.query db "SELECT 0") 3)
           #(pull-plays db))))

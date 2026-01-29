(ns http-test
  (:require
    [http :as h]
    [cljs.test :refer [deftest is]]))

(deftest map->params
  (is (= (h/map->params {:banana 1 :apple true :cherry [1 2]})
         "banana=1&apple=true&cherry=1%2C2")))

(ns middleware
  (:require
    ["pg" :refer [Client]]
    [macro :refer [if-not-p]]
    [error :as err]
    [static :refer [credentials]]))

(defn with-database [handler]
  (fn [req res]
    (let [client (Client. credentials)]
      (if-not-p [[connection-error _] (.connect client)]
        (err/generic connection-error res 500)
        (-> req
            (js/Object.assign #js{:database client})
            (handler res)
            (.finally #(.end client)))))))

(defn with-header [handler header value]
  (fn [req res]
    (.set res header value)
    (handler req res)))

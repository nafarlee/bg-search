(ns middleware
  (:require
    ["pg" :refer [Client]]
    [promise :refer [then-not]]
    [error :as err]
    [static :refer [credentials]]))

(defn with-database [handler]
  (fn [req res]
    (let [client (Client. credentials)]
      (then-not (.connect client)
        #(err/generic % res 500)
        (fn [] (-> req
                   (js/Object.assign #js{:database client})
                   (handler res)
                   (.finally #(.end client))))))))

(defn with-header [handler header value]
  (fn [req res]
    (.set res header value)
    (handler req res)))

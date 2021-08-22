(ns middleware
  (:require
    [clojure.string :refer [join]]
    [clojure.set :refer [difference]]
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

(defn with-required-body-parameters [handler required]
  (fn [req res]
    (let [qps  (set (keys (js->clj (.-body req))))
          diff (difference required qps)]
      (if (= diff #{})
        (handler req res)
        (-> res
            (.status 422)
            (.send (str "Missing required query parameters: "
                        (join ", " diff))))))))

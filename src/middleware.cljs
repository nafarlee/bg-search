(ns middleware
  (:require
    [cljs.core.async :refer [go <!]]
    [cljs.core.async.interop :refer-macros [<p!]]
    ["pg" :refer [Client]]
    [error :as err]
    [static :refer [credentials]]))

(defn with-database [handler]
  (fn [req res]
    (let [client (Client. credentials)]
      (.then (.connect client)
             #(go
               (<! (handler (js/Object.assign req #js{:database client}) res))
               (<p! (.end client)))
             #(err/generic % res 500)))))

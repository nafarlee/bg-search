(ns util
 (:require
  (http :refer [sleep])))

(defn with-retry
  ([f n] (with-retry f n 1000))
  ([f n ms]
   (-> (f)
       (.catch (fn [e]
                 (if (zero? n)
                   (throw e)
                   (do
                     (println "Retrying after" ms "ms...")
                     (-> (sleep ms)
                         (.then #(with-retry f (dec n) (* ms 2)))))))))))

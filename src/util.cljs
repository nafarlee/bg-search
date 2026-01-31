(ns util
 (:require
  [promise :refer [wait]]))

(defn with-retry
  ([f n] (with-retry f n 1000))
  ([f n ms]
   (-> (f)
       (.catch (fn [e]
                 (if (zero? n)
                   (throw e)
                   (do
                     (println "Retrying after" ms "ms...")
                     (-> (wait ms)
                         (.then #(with-retry f (dec n) (* ms 2)))))))))))

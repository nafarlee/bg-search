(ns macro)

(defmacro if-not-p [[[bad good] p] on-error on-success]
 `(.then ~p
     (fn [s#]
       (let [~good s#]
         ~on-success))
     (fn [e#]
       (let [~bad e#]
         ~on-error))))

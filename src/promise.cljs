(ns promise)

(defn then-not [p on-failed on-resolved]
  (.then p on-resolved on-failed))

(ns sql.insert
  (:require
    [sql :refer [clj->sql]]))

(defn generate [table columns uniques chunks]
  (let [chunk->row #(as-> % $
                          (map hash-set $)
                          (interpose "," $)
                          (concat "(" $ ")"))
        values     (->> chunks
                        (map chunk->row)
                        (interpose ",")
                        flatten
                        (apply clj->sql))
        updates    (->> columns
                        (map #(vector % := (str "EXCLUDED." %)))
                        (interpose ",")
                        flatten
                        (apply clj->sql))]
    (clj->sql :insert :into table (list columns)
              :values values
              :on :conflict (list uniques)
              :do :update :set updates)))

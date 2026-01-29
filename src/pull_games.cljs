(ns pull-games
 (:require
  [cljs.core.async :refer [go-loop]]
  [cljs.core.async.interop :refer-macros [<p!]]
  [api :refer [get-games]]
  [util :refer [with-retry]]
  [sql.insert :refer [insert]]
  [sql :refer [get-game-checkpoint
               get-game-id-cliff
               insert-games
               mobius-games
               pool]]))

(defn pull-games [db api-key]
    (go-loop [checkpoint (<p! (get-game-checkpoint db))]
      (let [new-checkpoint (+ 20 checkpoint)]
        (if-not (try
                  (let [games (<p! (get-games api-key
                                              (range checkpoint
                                                     new-checkpoint)))]
                    (<p! (insert-games db (insert games) new-checkpoint))
                    (prn {:message        :insert-games
                          :checkpoint     checkpoint
                          :new-checkpoint new-checkpoint
                          :game-count     (count games)})
                    true)
                  (catch :default e
                    (js/console.error e)))
          (recur checkpoint)
          (recur (try
                   (let [cliff (<p! (get-game-id-cliff db))]
                     (if (< new-checkpoint cliff)
                       new-checkpoint
                       (do
                         (<p! (mobius-games db))
                         (prn {:message    :reset-game-checkpoint
                               :cliff      cliff
                               :checkpoint new-checkpoint})
                         1)))
                   (catch :default e
                     (js/console.error e)
                     new-checkpoint)))))))

(defn main []
  (let [db (pool)]
    (.then (with-retry #(.query db "SELECT 0") 3)
           #(pull-games db js/process.env.BGG_API_KEY))))

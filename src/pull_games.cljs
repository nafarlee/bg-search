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

(defn pull-games [db]
  (go-loop []
    (try
      (<p! (with-retry #(.query db "SELECT 0") 3))
      (let [checkpoint     (<p! (get-game-checkpoint db))
            new-checkpoint (+ 20 checkpoint)
            cliff          (<p! (get-game-id-cliff db))
            games          (<p! (get-games (range checkpoint new-checkpoint)))
            insertions     (insert games)]
        (<p! (insert-games db insertions new-checkpoint))
        (when (< cliff new-checkpoint)
          (<p! (mobius-games db)))
        (prn {:checkpoint     checkpoint
              :new-checkpoint new-checkpoint
              :cliff          cliff
              :games-inserted (count games)}))
      (catch js/Error err
        (js/console.error err)))
    (recur)))

(defn main []
  (pull-games (pool)))

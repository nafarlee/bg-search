(ns pull-games
 (:require
  [api :refer [get-games]]
  [util :refer [with-retry]]
  [sql.insert :refer [insert]]
  [sql :refer [get-game-checkpoint
               get-game-id-cliff
               insert-games
               mobius-games
               pool]]))

(defn pull-games [db]
  (let [checkpoint-p
        (with-retry #(get-game-checkpoint db) 3)

        new-checkpoint-p
        (.then checkpoint-p
               #(+ % 20))

        cliff-p
        (with-retry #(get-game-id-cliff db) 3)

        games-p
        (.then (js/Promise.all #js[checkpoint-p new-checkpoint-p])
               (fn [[checkpoint new-checkpoint]]
                 (get-games (range checkpoint new-checkpoint))))
        
        insertions-p
        (.then games-p #(insert %))

        did-insert-p?
        (.then (js/Promise.all #js[insertions-p new-checkpoint-p])
               (fn [[insertions new-checkpoint]]
                 (insert-games db insertions new-checkpoint)))

        mobius-p
        (.then (js/Promise.all #js[did-insert-p? new-checkpoint-p cliff-p])
               (fn [[_ new-checkpoint cliff]]
                 (when (< cliff new-checkpoint)
                   (mobius-games db))))]
    (-> mobius-p
        (.then (fn [_]
                 (pull-games db)
                 nil))
        (.catch js/console.error))))
  

(defn main []
  (pull-games (pool)))

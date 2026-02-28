(ns pull-plays
 (:require
  [cljs.core.async :refer [go-loop]]
  [cljs.core.async.interop :refer-macros [<p!]]
  [api :refer [get-plays]]
  [sql :refer [game? 
               get-last-game
               get-plays-checkpoint
               play?
               pool
               save-plays
               update-plays-checkpoint]]
  [util :refer [with-retry]]))

(defn pull-plays [db api-key]
  (go-loop [[game-id page] (<p! (get-plays-checkpoint db))]
    (cond
      (< (<p! (get-last-game db)) game-id)
      (do
        (<p! (update-plays-checkpoint db 1 1))
        (prn :surpassed-game-id {:game-id game-id})
        (recur [1 1]))

      (not (<p! (game? db game-id)))
      (do
        (<p! (update-plays-checkpoint db (inc game-id) 1))
        (prn :not-a-game {:game-id game-id})
        (recur [(inc game-id) 1]))

      :else
      (let [plays (<p! (get-plays api-key game-id page))]
        (cond
          (empty? plays)
          (do
            (<p! (update-plays-checkpoint db (inc game-id) 1))
            (prn :no-plays {:game-id game-id :page page})
            (recur [(inc game-id) 1]))

          ; Temporary short-circuit to allow re-pulling existing plays
          (and false (<p! (play? db (-> plays first :id))))
          (do
            (<p! (update-plays-checkpoint db (inc game-id) 1))
            (prn :no-new-plays {:game-id game-id :page page})
            (recur [(inc game-id) 1]))

          :else
          (do
            (<p! (save-plays db game-id page plays))
            (prn :save-plays {:game-id game-id :page page})
            (recur [game-id (inc page)])))))))

(defn main []
  (let [db (pool)]
    (.then (with-retry #(.query db "SELECT 0") 3)
           #(pull-plays db js/process.env.BGG_API_KEY))))

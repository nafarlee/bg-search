(ns set-game-id-cliff
 (:require
  [sql.dsl :refer [clj->sql]]
  [api :refer [get-games]]))


(defn- filter-valid-ids [ids]
  (-> (get-games ids)
      (.then (fn [games]
               (when (seq games)
                 (into #{} (map :id) games))))))


(defn- range-between [start end]
  (let [delta (- end start)]
    (range start
           end
           (if (<= delta 20)
               1
               (js/Math.ceil (/ delta 20))))))


(defn- middle [x y]
  (-> (+ x y)
      (/ 2)
      js/Math.floor))


(defn- exponential-search
  ([] (exponential-search 1 10))
  ([current potential]
   (prn :exponential-search {:current current :potential potential})
   (-> (filter-valid-ids (range-between current potential))
       (.then (fn [valid-ids]
                (if valid-ids
                    (exponential-search potential (* potential 10))
                    current))))))


(defn- binary-search
  ([top] (binary-search 1 top))
  ([bottom top]
   (let [mid (middle bottom top)]
     (prn :binary-search {:bottom bottom :mid mid :top top})
     (if (or (= bottom mid) (= mid top))
         mid
         (-> (filter-valid-ids (range-between mid top))
             (.then (fn [valid-ids]
                      (if valid-ids
                          (binary-search (apply max valid-ids) top)
                          (binary-search bottom mid)))))))))


(defn- find-max-id []
  (-> (exponential-search)
      (.then binary-search)))


(defn- set-cliff [db cliff]
  (.query db (clj->sql :UPDATE :kv
                       :SET :value := #{cliff}
                       :WHERE :key := :game-id-cliff)))


(defn main []
  (-> (find-max-id)
      (.then prn)
      (.catch prn)))

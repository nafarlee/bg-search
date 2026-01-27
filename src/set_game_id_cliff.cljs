(ns set-game-id-cliff
 (:require
  [sql :refer [pool]]
  [api :refer [get-games]]))


(defn- filter-valid-ids [api-key ids]
  (-> (get-games api-key ids)
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
  ([api-key] (exponential-search api-key 1 10))
  ([api-key current potential]
   (prn :exponential-search {:current current :potential potential})
   (-> (filter-valid-ids api-key (range-between current potential))
       (.then (fn [valid-ids]
                (if valid-ids
                    (exponential-search api-key potential (* potential 10))
                    current))))))


(defn- binary-search
  ([api-key top] (binary-search api-key 1 top))
  ([api-key bottom top]
   (let [mid (middle bottom top)]
     (prn :binary-search {:bottom bottom :mid mid :top top})
     (if (or (= bottom mid) (= mid top))
         mid
         (-> (filter-valid-ids api-key (range-between mid top))
             (.then (fn [valid-ids]
                      (if valid-ids
                          (binary-search api-key (apply max valid-ids) top)
                          (binary-search api-key bottom mid)))))))))


(defn- find-max-id [api-key]
  (-> (exponential-search js/process.env.BGG_API_KEY)
      (.then #(binary-search api-key %))))


(defn- set-cliff [db cliff]
  (println "Setting game ID cliff to " cliff)
  (-> (.query db
              "UPDATE kv SET value = $1 WHERE key = 'game-id-cliff'"
              #js[cliff])
      (.then #(println "Successfully set game ID cliff to " cliff))))


(defn main []
  (let [db (pool)]
    (-> (find-max-id js/process.env.BGG_API_KEY)
        (.then #(set-cliff db %))
        (.catch prn))))

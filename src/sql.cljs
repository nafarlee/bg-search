(ns sql
  (:require
    [clojure.string :as s]
    ["/db/insert" :refer [toSQL]]))

(defn clj->sql [& tokens]
  (letfn [(map-token [token]
            (cond
              ((some-fn keyword? string?) token)
              {:text [(name token)]
               :values []}

              (and (contains? token :text) (contains? token :values))
              token

              (nil? token)
              {:text []
               :values []}

              (set? token)
              {:text [:?]
               :values [(first token)]}

              (list? token)
              (update (reduce-tokens token)
                      :text
                      #(vec (concat "(" % ")")))

              (vector? token)
              {:values []
               :text (conj (mapv (comp #(str % ",") name)
                                 (butlast token))
                           (name (last token)))}))
          (reduce-tokens [tokens]
            (reduce #(merge-with (comp vec concat) %1 (map-token %2))
                             {:text [] :values []}
                             tokens))]
    (reduce-tokens tokens)))

(defn realize-query [{:keys [text values]}]
  (let [parameter-index (atom 1)]
    {:values values
     :text (->> text
                (mapv #(if (not= :? %)
                         %
                         (do
                           (swap! parameter-index inc)
                           (str "$" (dec @parameter-index)))))
                (s/join " "))}))

(defn query [database q]
  (->> q
       realize-query
       clj->js
       (.query database)))

(def ^:private get-game-sql
  "SELECT
    (SELECT JSON_OBJECT_AGG(players, JSON_BUILD_OBJECT('median', median, 'count', count))
     FROM play_medians
     WHERE game_id = $1) as median_playtimes,
    (SELECT ARRAY_AGG(mechanic)
      FROM mechanics
      INNER JOIN games_mechanics ON id = mechanic_id
      WHERE game_id = $1) AS mechanics,
    (SELECT ARRAY_AGG(category)
      FROM categories
      INNER JOIN games_categories ON id = category_id
      WHERE game_id = $1) AS categories,
    (SELECT ARRAY_AGG(family)
      FROM families
      INNER JOIN games_families ON id = family_id
      WHERE game_id = $1) AS families,
    (SELECT ARRAY_AGG(publisher)
      FROM publishers
      INNER JOIN games_publishers ON id = publisher_id
      WHERE game_id = $1) AS publishers,
    (SELECT ARRAY_AGG(artist)
      FROM artists
      INNER JOIN games_artists ON id = artist_id
      WHERE game_id = $1) AS artists,
    (SELECT ARRAY_AGG(designer)
      FROM designers
      INNER JOIN games_designers ON id = designer_id
      WHERE game_id = $1) AS designers,
    (SELECT ARRAY_AGG(alternate_name)
      FROM alternate_names
      WHERE id = $1) AS alternate_names,
    (SELECT JSON_AGG(player_recommendations)
      FROM player_recommendations
      WHERE id = $1) AS player_recommendations,
    games.id,
    image,
    average_rating,
    average_weight,
    bayes_rating,
    steamdb_rating,
    description,
    maximum_players,
    maximum_playtime,
    minimum_age,
    minimum_players,
    minimum_playtime,
    primary_name,
    rating_deviation,
    rating_votes,
    weight_votes,
    year
  FROM games
  WHERE games.id = $1
  GROUP BY games.id")

(defn get-game [database id]
  (-> database
      (.query get-game-sql #js[id])
      (.then #(-> % .-rows first))))

(defn play? [database id]
  (-> database
      (.query "SELECT id FROM plays WHERE id = $1 LIMIT 1" #js[id])
      (.then #(some-> % .-rows first .-id))))

(defn game? [database id]
  (-> database
      (.query "SELECT id FROM games WHERE id = $1 LIMIT 1" #js[id])
      (.then #(some-> % .-rows first .-id))))

(defn get-last-game [database]
  (-> database
      (.query "SELECT id FROM games ORDER BY id DESC LIMIT 1")
      (.then #(-> % .-rows first .-id))))

(defn get-game-checkpoint [database]
  (-> database
      (.query "SELECT count FROM globals")
      (.then #(-> % .-rows first .-count))))

(defn get-plays-checkpoint [database]
  (-> database
      (.query "SELECT play_id, play_page FROM globals")
      (.then (fn [result]
               (-> result
                   .-rows
                   first
                   ((juxt
                     (fn [^js o] (.-play_id o))
                     (fn [^js o] (.-play_page o)))))))))

(defn begin [database]
  (.query database "BEGIN"))

(defn commit [database]
  (.query database "COMMIT"))

(defn rollback [database]
  (.query database "ROLLBACK"))

(defn update-game-checkpoint [database checkpoint]
  (.query database "UPDATE globals SET count = $1 WHERE id = $2" #js[checkpoint 1]))

(defn mobius-games [database]
  (update-game-checkpoint database 1))

(defn update-plays-checkpoint [database play-id play-page]
  (.query database
          "UPDATE globals SET play_id=$1, play_page=$2 WHERE id=1"
          #js[play-id play-page]))

(defn mobius-plays [database]
  (update-plays-checkpoint database 1 1))

(defn save-plays [database play-id play-page plays]
  (-> (begin database)
      (.then #(update-plays-checkpoint database play-id (inc play-page)))
      (.then (fn [_]
               (let [[sql values] (toSQL "plays"
                                         #js["id" "game_id" "length" "players"]
                                         #js["id"]
                                         plays)]
                 (.query database sql values))))
      (.then #(commit database))
      (.catch (fn [error] (-> (rollback database)
                              (.then #(js/Promise.reject error)))))))

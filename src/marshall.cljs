(ns marshall)

(defn- get-name [{:strs [$_name]}] $_name)

(defn- get-type [{:strs [$_type]}] $_type)

(defn- get-value [{:strs [$_value]}] $_value)

(defn- get-id [{:strs [$_id]}] $_id)


(defn- get-int-in [m path]
  (-> m
      (get-in path "0")
      (js/parseInt 10)))

(defn- get-float-in [m path]
  (-> m
      (get-in path "0")
      js/parseFloat))

(defn- id-bundle [x]
  {:id    (-> x get-id (js/parseInt 10))
   :value (get-value x)})

(def ^:private language-dependencies
  {"No necessary in-game text"                                        0
   "Some necessary text - easily memorized or small crib sheet"       1
   "Moderate in-game text - needs crib sheet or paste ups"            2
   "Extensive use of text - massive conversion needed to be playable" 3
   "Unplayable in another language"                                   4})

(defn- marshall-game [game]
  {:api-version
   3

   :last-updated
   (.toISOString (js/Date.))

   :id
   (get-int-in game ["$_id"])

   :language-dependence
   (->> (get game "poll")
        (filter (comp (partial = "language_dependence") get-name))
        (filter (comp pos? #(get-int-in % ["$_totalvotes"])))
        (map #(get-in % ["results" "result"]))
        (map (fn [polls]
               (apply max-key #(get-int-in % ["$_numvotes"]) polls)))
        (map get-value)
        (map (partial get language-dependencies))
        first)

   :image
   (get game "image")

   :thumbnail
   (get game "thumbnail")

   :primary-name
   (let [name' (get game "name")]
     (if (map? name')
       (get name' "$_value")
       (->> name'
            (filter (comp (partial = "primary") get-type))
            (map get-value)
            first)))

   :alternate-names
   (let [name' (get game "name")]
     (when-not (map? name')
       (->> (get game "name")
            (filter (comp (partial = "alternate") get-type))
            (map get-value))))

   :description
   (get game "description")

   :year
   (get-int-in game ["yearpublished" "$_value"])

   :minimum-players
   (get-int-in game ["minplayers" "$_value"])

   :maximum-players
   (get-int-in game ["maxplayers" "$_value"])

   :community-recommended-players
   (let [m                        (->> (get game "poll")
                                       (filter (comp #(= % "suggested_numplayers")
                                                     #(get % "$_name")))
                                       first)
         total-votes              (js/parseInt (get m "$_totalvotes") 10)
         results                  (get m "results")
         normalize-recommendation (fn [{:strs [$_value $_numvotes]}]
                                    {(case $_value
                                           "Best" :best
                                           "Recommended" :recommended
                                           "Not Recommended" :not-recommended)
                                     (js/parseInt $_numvotes 10)})
         normalize-results        (fn [{:strs [$_numplayers result]}]
                                    {$_numplayers
                                     (apply merge (map normalize-recommendation result))})]
     (when-not (zero? total-votes)
         {:votes  total-votes
          :counts (if (map? results)
                    (normalize-results results)
                    (apply merge (map normalize-results results)))}))

   :minimum-playtime
   (get-int-in game ["minplaytime" "$_value"])

   :maximum-playtime
   (get-int-in game ["maxplaytime" "$_value"])

   :minimum-age
   (get-int-in game ["minage" "$_value"])

   :rating-votes
   (get-int-in game ["statistics" "ratings" "usersrated" "$_value"])

   :average-rating
   (get-float-in game ["statistics" "ratings" "average" "$_value"])

   :bayes-rating
   (get-float-in game ["statistics" "ratings" "bayesaverage" "$_value"])

   :rating-deviation
   (get-float-in game ["statistics" "ratings" "stddev" "$_value"])

   :weight-votes
   (get-int-in game ["statistics" "ratings" "numweights" "$_value"])

   :average-weight
   (get-float-in game ["statistics" "ratings" "averageweight" "$_value"])

   :categories
   (->> (get game "link")
        (filter (comp (partial = "boardgamecategory") get-type))
        (map id-bundle))

   :mechanics
   (->> (get game "link")
        (filter (comp (partial = "boardgamemechanic") get-type))
        (map id-bundle))

   :families
   (->> (get game "link")
        (filter (comp (partial = "boardgamefamily") get-type))
        (map id-bundle))

   :expanded-by
   (->> (get game "link")
        (filter (comp (partial = "boardgameexpansion") get-type))
        (remove #(get % "$_inbound"))
        (map id-bundle))

   :expands
   (->> (get game "link")
        (filter (comp (partial = "boardgameexpansion") get-type))
        (filter #(get % "$_inbound"))
        (map id-bundle))

   :contained-in
   (->> (get game "link")
        (filter (comp (partial = "boardgamecompilation") get-type))
        (remove #(get % "$_inbound"))
        (map id-bundle))

   :contains
   (->> (get game "link")
        (filter (comp (partial = "boardgamecompilation") get-type))
        (filter #(get % "$_inbound"))
        (map id-bundle))

   :reimplemented-by
   (->> (get game "link")
        (filter (comp (partial = "boardgameimplementation") get-type))
        (remove #(get % "$_inbound"))
        (map id-bundle))

   :reimplements
   (->> (get game "link")
        (filter (comp (partial = "boardgameimplementation") get-type))
        (filter #(get % "$_inbound"))
        (map id-bundle))

   :integrates-with
   (->> (get game "link")
        (filter (comp (partial = "boardgameintegration") get-type))
        (map id-bundle))

   :designers
   (->> (get game "link")
        (filter (comp (partial = "boardgamedesigner") get-type))
        (map id-bundle))

   :artists
   (->> (get game "link")
        (filter (comp (partial = "boardgameartist") get-type))
        (map id-bundle))

   :publishers
   (->> (get game "link")
        (filter (comp (partial = "boardgamepublisher") get-type))
        (map id-bundle))})

(defn marshall [game]
  (->> game
       marshall-game
       (remove (comp #(js/Number.isNaN %) second))
       (remove (comp nil? second))
       (into {})))

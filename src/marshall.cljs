(ns marshall)

(defn get-type [{:strs [$_type]}] $_type)

(defn get-value [{:strs [$_value]}] $_value)

(defn get-id [{:strs [$_id]}] $_id)

(defn get-number-in [m path]
  (-> m
      (get-in path "0")
      (js/parseInt 10)))

(defn id-bundle [x]
  {:id    (-> x get-id (js/parseInt 10))
   :value (get-value x)})

(defn marshall [game]
  {:api-version
   3

   :id
   (get-number-in game ["$_id"])

   :image
   (get game "image")

   :thumbnail
   (get game "thumbnail")

   :primary-name
   (->> (get game "name")
        (filter (comp (partial = "primary") get-type))
        (map get-value)
        first)

   :alternate-names
   (->> (get game "name")
        (filter (comp (partial = "alternate") get-type))
        (map get-value))

   :description
   (get game "description")

   :year
   (get-number-in game ["yearpublished" "$_value"])

   :minimum-players
   (get-number-in game ["minplayers" "$_value"])

   :maximum-players
   (get-number-in game ["maxplayers" "$_value"])

   :community-recommended-players nil

   :minimum-playtime
   (get-number-in game ["minplaytime" "$_value"])

   :maximum-playtime
   (get-number-in game ["maxplaytime" "$_value"])

   :minimum-age
   (get-number-in game ["minage" "$_value"])

   :rating-votes
   (get-number-in game ["statistics" "ratings" "usersrated" "$_value"])

   :average-rating
   (get-number-in game ["statistics" "ratings" "average" "$_value"])

   :bayes-rating
   (get-number-in game ["statistics" "ratings" "bayesaverage" "$_value"])

   :rating-deviation
   (get-number-in game ["statistics" "ratings" "stddev" "$_value"])

   :weight-votes
   (get-number-in game ["statistics" "ratings" "numweights" "$_value"])

   :average-weight
   (get-number-in game ["statistics" "ratings" "averageweight" "$_value"])

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

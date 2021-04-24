(ns marshall)

(defn get-type [{:strs [$_type]}] $_type)

(defn get-value [{:strs [$_value]}] $_value)

(defn get-number-in [m path]
  (-> m
      (get-in path "0")
      (js/parseInt 10)))

(def primary-name? (partial = "primary"))

(def alternate-name? (partial = "alternate"))

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
        (filter (comp primary-name? get-type))
        (map get-value)
        first)

   :alternate-names
   (->> (get game "name")
        (filter (comp alternate-name? get-type))
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

   :weight-votes nil
   :average-weight nil
   :categories nil
   :mechanics nil
   :families nil
   :expanded-by nil
   :contained-in nil
   :reimplemented-by nil
   :designers nil
   :artists nil
   :publishers nil})

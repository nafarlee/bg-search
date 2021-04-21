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

   :minimum-players nil
   :maximum-players nil
   :community-recommended-players nil
   :minimum-playtime nil
   :maximum-playtime nil
   :minimum-age nil
   :rating-votes nil
   :average-rating nil
   :bayes-rating nil
   :rating-deviation nil
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

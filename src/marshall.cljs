(ns marshall)

(defn primary-name? [{:strs [$_type]}]
  (= "primary" $_type))

(defn marshall [game]
  {:api-version
   3

   :id
   (get game "$_id")

   :image
   (get game "image")

   :thumbnail
   (get game "thumbnail")

   :primary-name
   (->> (get game "name")
        (filter primary-name?)
        (map #(get % "$_value"))
        first)

   :alternate-names nil
   :description nil
   :year nil
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

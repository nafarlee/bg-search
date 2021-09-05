(ns component)

(def head
  (list
   [:link
    {:rel "stylesheet"
     :href "https://fonts.googleapis.com/css?family=Roboto:300,300italic,700,700italic"}]
   [:link
    {:rel "stylesheet"
     :href "https://cdnjs.cloudflare.com/ajax/libs/normalize/8.0.1/normalize.css"}]
   [:link
    {:rel "stylesheet"
     :href "https://cdnjs.cloudflare.com/ajax/libs/milligram/1.4.1/milligram.css"}]
   [:link
    {:rel "stylesheet"
     :type "text/css"
     :href "/style.css"}]
   [:meta
    {:name "viewport"
     :content "width=device-width, initial-scale=1"}]))

(defn options [selected m]
  (map (fn [[k v]]
         [:option {:value (name k) :selected (= (name k) selected)} v])
       m))

(defn search [{:keys [query order direction]}]
  (let [possible-orders {:id "ID"
                         :primary_name "Name"
                         :rating_votes "Number of Ratings"
                         :average_rating "Average Rating"
                         :steamdb_rating "SteamDB Rating"
                         :bayes_rating "Geek Rating"
                         :rating_deviation "Rating Deviation"
                         :average_weight "Weight"
                         :weight_votes "Number of Weight Ratings"
                         :year "Release Year"
                         :age "Minimum Age"
                         :minimum_players "Minimum Players"
                         :maximum_players "Maximum Players"
                         :minimum_playtime "Minimum Playtime"
                         :maximum_playtime "Maximum Playtime"}
        possible-directions {:DESC "Descending"
                             :ASC "Ascending"}
        search-attributes {:spellcheck "false"
                           :autocomplete "off"
                           :autocapitalize "off"
                           :autocorrect "off"
                           :value query
                           :type "search"
                           :mozactionhint "search"
                           :name "query"}]
    [:form {:method "get" :action "/search"}
     [:div {:class "row"}
      [:div {:class "column"}
       [:input search-attributes]]]
     [:div {:class "row"}
      [:div {:class "column"}
       [:label {:for "order"} "Sort"]
       [:select {:name "order"}
        (options order possible-orders)]]
      [:div {:class "column"}
       [:label {:for "direction"} "Direction"]
       [:select {:name "direction"}
          (options direction possible-directions)]]]]))

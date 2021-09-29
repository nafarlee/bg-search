(ns component
  (:require
    [markdown.core :refer [md->html]]))

(def head
  (list
   [:link
    {:rel "stylesheet"
     :href "https://cdn.jsdelivr.net/npm/water.css@2/out/water.css"}]
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
                           :placeholder "n:catan"
                           :autocomplete "off"
                           :autocapitalize "off"
                           :autocorrect "off"
                           :value query
                           :type "search"
                           :mozactionhint "search"
                           :name "query"}]
    [:form {:method "get" :action "/search"}
     [:div
      [:div
       [:input.box-border.w-full search-attributes]]]
     [:div.flex.flex-wrap
      [:div.flex-grow
       [:label {:for "order"} "Sort"]
       [:select.min-w-max.w-full {:name "order"}
        (options order possible-orders)]]
      [:div.flex-grow
       [:label {:for "direction"} "Direction"]
       [:select.min-w-max.w-full {:name "direction"}
        (options direction possible-directions)]]]
     [:div
      [:input {:type "submit" :value "Search"}]]]))

(defn term [{:keys [term alias description example]}]
  [:tr
   [:td (name term)]
   [:td (when alias [:code (name alias)])]
   [:td (md->html description)]
   [:td (md->html example)]])

(ns view
  (:require
    [clojure.string :as s]
    [html :refer [html doctype]]))

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

(defn search [{:keys [games query next-url direction order]}]
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
                           :name "query"}
        game->heading (fn [{:strs [id year primary_name]}]
                        [:p [:a {:href (str "/games/" id)}
                             (str primary_name " (" year ")")]])]
    (html
     (list
      doctype
      [:html
       [:head head]
       [:body {:class "container"}
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
            (options direction possible-directions)]]]]
        (if (empty? games)
          [:h1 {:class "center"} "No more results!"]
          (list
           (map game->heading games)
           [:br]))
        (when (= 25 (count games))
          [:p [:a {:href next-url} "Next"]])]]))))

(defn error [{:keys [code message block]}]
  (html
   (list
    doctype
    [:html
     [:head head]
     [:body {:class "container"}
      [:h1 {:class "center"} code]
      [:h3 {:class "center"} message]
      (when block
        [:pre [:code block]])]])))

(defn games
  [{:strs [primary_name
           median_playtimes
           minimum_playtime
           maximum_playtime
           minimum_players
           maximum_players
           weight_votes
           average_weight
           categories
           mechanics
           families
           designers
           publishers
           artists
           alternate_names
           year
           id
           description
           image
           rating_votes
           average_rating
           bayes_rating
           steamdb_rating
           rating_deviation]}]
  (let [maybe-details-list (fn [heading coll]
                             (when coll
                               [:details
                                [:summary heading]
                                [:ul (map #(vector :li %) coll)]]))
        render-median-playtime (fn [[players {:strs [median count]}]]
                                 (if (= "0" players)
                                   [:li (str "All Player Counts: " median " minutes across " count " plays")]
                                   [:li (str players " Player(s): " median " minutes across " count " plays")]))]
    (html
     (list
      doctype
      [:html
       [:head
        head
        [:title primary_name]]
       [:body {:class "container"}
        [:h1 {:class "center"} (str primary_name " (" year ")")]
        [:h2 [:a {:href (str "https://boardgamegeek.com/boardgame/" id)} "BGG"]]
        [:h2 [:a {:href image} "Image"]]
        (when description
          [:details
           [:summary "Description"]
           (map (fn [p] [:p p])
                (s/split description "&amp;#10;&amp;#10;"))])
        [:details
         [:summary "Rating"]
         [:ul
          [:li (str "Votes: " rating_votes)]
          [:li (str "Average: " average_rating)]
          [:li (str "Bayes: " bayes_rating)]
          [:li (str "SteamDB " steamdb_rating)]
          [:li (str "Deviation: " rating_deviation)]]]
        [:details
         [:summary "Playtime"]
         [:ul
          [:li (str "Minimum: " minimum_playtime)]
          [:li (str "Maximum: " maximum_playtime)]
          (when median_playtimes
            (list
             [:li "Medians:"]
             [:ul (map render-median-playtime
                       (sort-by #(js/parseInt (first %) 10)
                                median_playtimes))]))]]
        [:details
         [:summary "Players"]
         [:ul
          [:li "Minimum: " minimum_players]
          [:li "Maximum: " maximum_players]]]
        [:details
         [:summary "Player Recommendations"]
         [:h1 "TODO"]]
        [:details
         [:summary "Weight"]
         [:ul
          [:li "Votes: " weight_votes]
          [:li "Average: " average_weight]]]
        (maybe-details-list "Categories" categories)
        (maybe-details-list "Mechanics" mechanics)
        (maybe-details-list "Families" families)
        (maybe-details-list "Designers" designers)
        (maybe-details-list "Publishers" publishers)
        (maybe-details-list "Artists" artists)
        (maybe-details-list "Alternate Names" alternate_names)]]))))

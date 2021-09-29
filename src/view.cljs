(ns view
  (:require
    [term :refer [terms]]
    [component :as c]
    [clojure.string :as s]
    [html :refer [html doctype]]))

(defn- percentage-of
  ([numer denom]
   (percentage-of numer denom 1))
  ([numer denom decimals]
   (-> numer
       (/ denom)
       (* 100)
       (.toFixed decimals)
       (str "%"))))

(defn- range->text [r]
  (let [regex         #"\[(\d+),(\d*)\)"
        [_ start end] (re-matches regex r)]
    (str start (when (empty? end) "+"))))

(defn- sort-recommendations [recommendations]
  (sort-by #(-> % (get "players") range->text (js/parseInt 10))
           recommendations))

(defn search [{:keys [games query next-url direction order]}]
  (let [game->heading (fn [{:strs [id year primary_name]}]
                        [:p [:a {:href (str "/games/" id)}
                             (str primary_name " (" year ")")]])]
    (html
     (list
      doctype
      [:html
       [:head c/head]
       [:body
        (c/search-form {:query query :order order :direction direction})
        (if (empty? games)
          [:h1 "No more results!"]
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
     [:head c/head]
     [:body
      [:h1 code]
      [:h3 message]
      (when block
        [:pre [:code block]])]])))

(defn games
  [{:strs [primary_name
           player_recommendations
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
                                   [:li (str players " Player(s): " median " minutes across " count " plays")]))
        render-player-recommendation (fn [{:strs [players best recommended not_recommended]}]
                                       (let [total (+ best recommended not_recommended)]
                                         [:li
                                          (range->text players)
                                          [:ul
                                           [:li (str "Best: " (percentage-of best total) " (" best ")")]
                                           [:li (str "Recommended: " (percentage-of recommended total) " (" recommended ")")]
                                           [:li (str "Not Recommended: " (percentage-of not_recommended total) " (" not_recommended ")")]]]))]
    (html
     (list
      doctype
      [:html
       [:head
        c/head
        [:title primary_name]]
       [:body
        [:h1 (str primary_name " (" year ")")]
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
        (when player_recommendations
          [:details
           [:summary "Player Recommendations"]
           [:ul (map render-player-recommendation (sort-recommendations player_recommendations))]])
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

(defn index []
  (html
   (list
    doctype
    [:head
     c/head
     [:title "Board Game Search"]]
    [:body
     [:h1.text-center "Board Game Search"]
     (c/search-form {:order "bayes_rating" :direction "DESC"})
     c/collection-form
     c/tutorial
     [:h2 "Term Reference"]
     [:table
      [:thead
       [:tr
        [:th "Term"]
        [:th "Shortcuts"]
        [:th "Description"]
        [:th "Examples"]]]
      [:tbody (map c/term (sort-by :term terms))]]])))

(ns view
  (:require
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
       [:body {:class "container"}
        (c/search {:query query :order order :direction direction})
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
     [:head c/head]
     [:body {:class "container"}
      [:h1 {:class "center"} code]
      [:h3 {:class "center"} message]
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
    [:body {:class "container"}
     [:h1 {:class "center"} "Board Game Search"]
     (c/search {:order "bayes_rating" :direction "DESC"})
     [:form {:method "post" :action "/pull-collection"}
      [:div {:class "row"}
       [:div {:class "column column-75"}
        [:input {:name "username"}]]
       [:input {:class "column column-25" :type "submit" :value "Pull BGG Collection"}]]]
     [:h2 "Language"]
     [:p "A valid query in this language is comprised of any number of terms, combined or modified
          in certain ways. All terms may be:"]
     [:ul
      [:li "Separated by spaces for an " [:strong "AND"] " relationship"]
      [:li "Separated by the word \"or\" for an " [:strong "OR"] " relationship"]
      [:li "Grouped together by surrounding one or more with parentheses. This is useful in
            conjunction with the \"or\" separator"]
      [:li "Prefixed with a minus (-) to " [:strong "NEGATE"] " the terms results"]]
     [:div
      [:pre [:code "-is:expansion (mechanic:\"Dice Rolling\" best-players=2) or (category:economic best-players=4)"]]
      [:p "For example, the above query would match any games, which are "
          [:strong "not expansions"]
          ", and are "
          [:strong "either dice rolling"]
          " games that play "
          [:strong "best with 2 players"]
          ", "
          [:strong "or"]
          " are "
          [:strong "economic games"]
          " that play "
          [:strong "best with 4 players"]]
      [:p "Most terms have shortcuts, so if you like saving time, the above query could also be
           written as follows:"]
      [:pre [:code "-is:e (m:\"Dice Rolling\" bp=2) or (c:economic bp=4)"]]]
     [:p "There are also some behaviours that are specific to the type of the term:"]
     [:ul
      [:li "Most terms are separated by a colon (:), and can use quotes to specify values that
           contain spaces. Eg. " [:code "name:\"Race for the Galaxy\""]]
      [:li "Terms that work with numbers can use "
           [:strong "=, &gt;, &lt;, &gt;=, or &lt;="]
           " to specify relationships. Eg. "
           [:code "average-rating&gt;=7.5"]]
      [:li "Some terms always start with " [:strong "is"] ", and are always separated by a "
           [:strong "colon (:)"] ", but otherwise are only allowed to have specific values. Eg. "
           [:code "is:expansion"]]]
     [:h2 "Term Reference"]
     [:h3 "TODO"]])))

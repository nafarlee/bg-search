(ns component
  (:require
    [markdown.core :refer [md->html]]))

(defn head []
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

(defn query-form [{:keys [query order direction action submit-message]}]
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
    [:form {:method "get" :action action}
     [:input.box-border.w-full search-attributes]
     [:div.flex.flex-wrap.gap-2
      [:div.flex-grow
       [:label {:for "order"} "Sort"]
       [:select.min-w-max.w-full {:name "order"}
        (options order possible-orders)]]
      [:div.flex-grow
       [:label {:for "direction"} "Direction"]
       [:select.min-w-max.w-full {:name "direction"}
        (options direction possible-directions)]]]
     [:input.w-full {:type "submit" :value submit-message}]]))

(defn collection-form []
  [:form {:method "post" :action "/pull-collection"}
   [:input.box-border.w-full {:name "username"}]
   [:input.box-border.w-full {:type "submit" :value "Pull BGG Collection"}]])

(defn tutorial []
  (md->html
"
## Language

A valid query in this language is comprised of any number of terms, combined or modified in certain
ways. All terms may be:

- Separated by spaces for an **AND** relationship
- Separated by the word \"or\" for an **OR** relationship
- Grouped together by surrounding one or more with parentheses. This is useful in conjunction with
  the \"or\" separator
- Prefixed with a minus (-) to **NEGATE** the terms results


```
-is:expansion (mechanic:\"Dice Rolling\" best-players=2) or (category:economic best-players=4)
```

For example, the above query would match any games, which are **not expansions**, and are **either
dice rolling** games that play **best with 2 players**, **or** are **economic games** that play
**best with 4 players**

Most terms have shortcuts, so if you like saving time, the above query could also be written as
follows:


```
-is:e (m:\"Dice Rolling\" bp=2) or (c:economic bp=4)
```

There are also some behaviors that are specific to the type of the term:

- Most terms are separated by a colon (:), and can use quotes to specify values that contain
  spaces. Eg. `name:\"Race for the Galaxy\"`
- Terms that work with numbers can use **=, >, <, >=, or <=** to specify relationships. Eg.
  `average-rating>=7.5`
- Some terms always start with **is**, and are always separated by a **colon (:)**, but otherwise
  are only allowed to have specific values. Eg. `is:expansion`
"))

(defn term [{:keys [term alias description example]}]
  [:tr
   [:td (name term)]
   [:td (when alias [:code (name alias)])]
   [:td (md->html description)]
   [:td (md->html example)]])

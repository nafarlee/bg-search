(ns term
  (:require
    [markdown.core :refer [md->html]]))

(def terms
  #{{:term        :name
     :alias       :n
     :description (md->html "Matches a part of the games primary name")
     :example     (md->html "`name:catan` Games with **Catan** in the name")}

    {:term        :art
     :alias       :a
     :description (md->html "Matches a part of the games primary name")
     :example     (md->html "`art:\"jakub rozalski\"` Games where **Jakub Rozalski** supplied artwork")}

    {:term        :category
     :alias       :c
     :description (md->html "Matches a part of the name of any of the game's [categories](https://boardgamegeek.com/browse/boardgamecategory)")
     :example     (md->html "`category:act` Matches any game in the **Abstract Strategy**, **Industry / Manufacturing**, or **Action / Dexterity** categories")}

    {:term        :desc
     :description (md->html "Matches part of the game's description")
     :example     (md->html "`desc:flip` Matches any game that includes the word **flip** in its description")}

    {:term        :family
     :alias       :f
     :description (md->html "Matches a part of the name of any of the game's [families](https://boardgamegeek.com/browse/boardgamefamily)")
     :example     (md->html "`family:dominion` Matches any game in the **Dominion** family")}

    {:term        :mechanic
     :alias       :m
     :description (md->html "Matches a part of the name of any of the game's [mechanics](https://boardgamegeek.com/browse/boardgamemechanic)")
     :example     (md->html "`mechanic:building` Matches any game with **Pattern Building**, **Route/Network Building**, or **Deck / Pool Building** mechanics")}

    {:term        :publish
     :alias       :p
     :description (md->html "Matches a part of the name of any of the game's publishers")
     :example     (md->html "`publish:z-man` Matches any game published by **Z-Man Games**")}

    {:term        :design
     :alias       :desi
     :description (md->html "Matches a part of the name of any of the game's designers")
     :example     (md->html "`design:uwe` Matches any game designed by someone named **Uwe**")}

    {:term        :rating-votes
     :alias       :rv
     :description (md->html "Compares against the number of user ratings the game has received")
     :example     (md->html "`rating-votes>=1000` Matches any game with at least **1000** user ratings")}

    {:term        :average-rating
     :alias       :ar
     :description (md->html "Compares against the average user rating of a game")
     :example     (md->html "`average-rating<6.5` Matches any game with an average rating below **6.5**")}

    {:term        :geek-rating
     :alias       :gr
     :description (md->html "Compares against the game's BGG GeekRating metric")
     :example     (md->html "`geek-rating>=8.0` Matches any game with a GeekRating of at least **8.0**")}

    {:term        :rating-deviation
     :alias       :rd
     :description (md->html "Compares against the deviation in the game's ratings")
     :example     (md->html "`rating-deviation>=1.5` Matches against very divisive games")}

    {:term        :average-weight
     :alias       :aw
     :description (md->html "Compares against the game's average rated complexity")
     :example     (md->html "`average-weight>=3.7` Matches against games with an average weight of at least **3.7**")}

    {:term        :weight-votes
     :alias       :wv
     :description (md->html "Compares against the game's number of complexity ratings")
     :example     (md->html "`weight-votes<1000` Matches any games that have less than **100** weight votes")}

    {:term        :year
     :description (md->html "Matches the year a game was released")
     :example     (md->html "`year=2018 family:\"roll-and-write\"` All the new Roll-and-Write games")}

    {:term :age}

    {:term :rec-players
     :alias :rp}

    {:term :best-players
     :alias :bp}

    {:term :quorum-players
     :alias :qp}

    {:term :min-players
     :alias :mnpr}

    {:term :max-players
     :alias :mxpr}

    {:term :min-playtime
     :alias :mnpt}

    {:term :max-playtime
     :alias :mxpt}

    {:term :median-playtime
     :alias :mdpt}

    {:term :median-playtime-1
     :alias :mdpt1}

    {:term :median-playtime-2
     :alias :mdpt2}

    {:term :median-playtime-3
     :alias :mdpt3}

    {:term :median-playtime-4
     :alias :mdpt4}

    {:term :median-playtime-5
     :alias :mdpt5}

    {:term :is:expansion
     :alias :is:e}

    {:term :is:collection
     :alias :is:c}

    {:term :is:reimplementation
     :alias :is:r}

    {:term :own}})

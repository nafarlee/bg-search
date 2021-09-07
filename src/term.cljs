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
     :example     (md->html "`art:\"jakub rozalski\"` Games where **Jakub Rozalski** supplied
                             artwork")}

    {:term        :category
     :alias       :c
     :description (md->html "Matches a part of the name of any of the game's
                             [categories](https://boardgamegeek.com/browse/boardgamecategory)")
     :example     (md->html "`category:act` Matches any game in the **Abstract Strategy**,
                             **Industry / Manufacturing**, or **Action / Dexterity** categories")}

    {:term        :desc
     :description (md->html "Matches part of the game's description")
     :example     (md->html "`desc:flip` Matches any game that includes the word **flip** in its
                             description")}

    {:term        :family
     :alias       :f
     :description (md->html "Matches a part of the name of any of the game's
                             [families](https://boardgamegeek.com/browse/boardgamefamily)")
     :example     (md->html "`family:dominion` Matches any game in the **Dominion** family")}

    {:term        :mechanic
     :alias       :m
     :description (md->html "Matches a part of the name of any of the game's
                             [mechanics](https://boardgamegeek.com/browse/boardgamemechanic)")
     :example     (md->html "`mechanic:building` Matches any game with **Pattern Building**,
                             **Route/Network Building**, or **Deck / Pool Building** mechanics")}

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
     :example     (md->html "`rating-votes>=1000` Matches any game with at least **1000** user
                             ratings")}

    {:term        :average-rating
     :alias       :ar
     :description (md->html "Compares against the average user rating of a game")
     :example     (md->html "`average-rating<6.5` Matches any game with an average rating below
                             **6.5**")}

    {:term        :geek-rating
     :alias       :gr
     :description (md->html "Compares against the game's BGG GeekRating metric")
     :example     (md->html "`geek-rating>=8.0` Matches any game with a GeekRating of at least
                             **8.0**")}

    {:term        :rating-deviation
     :alias       :rd
     :description (md->html "Compares against the deviation in the game's ratings")
     :example     (md->html "`rating-deviation>=1.5` Matches against very divisive games")}

    {:term        :average-weight
     :alias       :aw
     :description (md->html "Compares against the game's average rated complexity")
     :example     (md->html "`average-weight>=3.7` Matches against games with an average weight of
                             at least **3.7**")}

    {:term        :weight-votes
     :alias       :wv
     :description (md->html "Compares against the game's number of complexity ratings")
     :example     (md->html "`weight-votes<1000` Matches any games that have less than **100**
                             weight votes")}

    {:term        :year
     :description (md->html "Matches the year a game was released")
     :example     (md->html "`year=2018 family:\"roll-and-write\"` All the new Roll-and-Write
                             games")}

    {:term        :age
     :description (md->html "Matches the game's minimum recommended player age")
     :example     (md->html "`age<=12` Matches any game that would be suitable for pre-teens")}

    {:term        :rec-players
     :alias       :rp
     :description (md->html "Matches against the number of players that are recommended by the BGG
                             community")
     :example     (md->html "`rec-players=3` Matches any game that plays well with 3 players")}

    {:term        :best-players
     :alias       :bp
     :description (md->html "Matches against the number of players that are best for a game as
                             determined by the BGG community")
     :example     (md->html "`best-players=3` Matches any game that plays best with 3 players")}

    {:term        :quorum-players
     :alias       :qp
     :description (md->html "Matches against games that play well at a particular player count
                             according to feedback given by a quorum (currently 70%) of the players
                             of said game. This quorum includes both **best** and **recommended**
                             player ratings")
     :example     (md->html "`quorum-players=3` Matches any game that plays well with 3 people as
                             rated by at least 4 out of 5 players")}

    {:term        :min-players
     :alias       :mnpr
     :description (md->html "Matches against the minimum number of required players")
     :example     (md->html "`min-players>=3` Matches any game that is playable with at least **3**
                             players")}

    {:term        :max-players
     :alias       :mxpr
     :description (md->html "Matches against the maximum number of players supported by a game")
     :example     (md->html "`min-players=2 max-players=5` Matches any game that plays exactly
                             **2** to **5** players")}

    {:term        :min-playtime
     :alias       :mnpt
     :description (md->html "Matches against the minimum number of minutes needed to play a game")
     :example     (md->html "`min-playtime>=480` Matches **Twilight Imperium**")}

    {:term        :max-playtime
     :alias       :mxpt
     :description (md->html "Matches against the maximum number of minutes needed to play a game")
     :example     (md->html "`max-playtime<=30` Matches any game you can definitely complete in a
                             half-hour")}

    {:term        :median-playtime
     :alias       :mdpt
     :description (md->html "Matches against the median number of minutes required to play a game,
                             as sourced by BGG user play data")
     :example     (md->html "`median-playtime<=60` Matches against any game that typically plays in an
                             hour or less")}

    {:term        :median-playtime-1
     :alias       :mdpt1
     :description (md->html "Matches against the median number of minutes required to play a game
                             with 1 player, as sourced by BGG user play data")
     :example     (md->html "`median-playtime-1<=60` Matches against any game that typically plays in
                             an hour or less with 1 players")}

    {:term        :median-playtime-2
     :alias       :mdpt2
     :description (md->html "Matches against the median number of minutes required to play a game
                             with 2 player, as sourced by BGG user play data")
     :example     (md->html "`median-playtime-1<=60` Matches against any game that typically plays in
                             an hour or less with 2 players")}

    {:term        :median-playtime-3
     :alias       :mdpt3
     :description (md->html "Matches against the median number of minutes required to play a game
                             with 3 player, as sourced by BGG user play data")
     :example     (md->html "`median-playtime-1<=60` Matches against any game that typically plays in
                             an hour or less with 3 players")}

    {:term        :median-playtime-4
     :alias       :mdpt4
     :description (md->html "Matches against the median number of minutes required to play a game
                             with 4 player, as sourced by BGG user play data")
     :example     (md->html "`median-playtime-1<=60` Matches against any game that typically plays in
                             an hour or less with 4 players")}

    {:term        :median-playtime-5
     :alias       :mdpt5
     :description (md->html "Matches against the median number of minutes required to play a game
                             with 5 player, as sourced by BGG user play data")
     :example     (md->html "`median-playtime-1<=60` Matches against any game that typically plays in
                             an hour or less with 5 players")}

    {:term        :is:expansion
     :alias       :is:e
     :description (md->html "Only matches against expansions")
     :example     (md->html "- `is:expansion family:carcassonne` Expansions for Carcassonne
                             - `-is:expansion` Only standalone games")}

    {:term        :is:collection
     :alias       :is:c
     :description (md->html "Only matches against games which are collections of other games")
     :example     (md->html "`is:collection family:carcassonne` Show any games that collects other
                             games in the Carcassonne family")}

    {:term :is:reimplementation
     :alias :is:r}

    {:term :own}})

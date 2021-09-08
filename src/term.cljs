(ns term)

(def terms
  #{{:term        :name
     :alias       :n
     :description "Matches a part of the games primary name"
     :example     "`name:catan` Games with **Catan** in the name"}

    {:term        :art
     :alias       :a
     :description "Matches a part of the games primary name"
     :example     "`art:\"jakub rozalski\"` Games where **Jakub Rozalski** supplied artwork"}

    {:term        :category
     :alias       :c
     :description "Matches a part of the name of any of the game's
                   [categories](https://boardgamegeek.com/browse/boardgamecategory)"
     :example     "`category:act` Matches any game in the **Abstract Strategy**,
                   **Industry / Manufacturing**, or **Action / Dexterity** categories"}

    {:term        :desc
     :description "Matches part of the game's description"
     :example     "`desc:flip` Matches any game that includes the word **flip** in its
                   description"}

    {:term        :family
     :alias       :f
     :description "Matches a part of the name of any of the game's
                   [families](https://boardgamegeek.com/browse/boardgamefamily)"
     :example     "`family:dominion` Matches any game in the **Dominion** family"}

    {:term        :mechanic
     :alias       :m
     :description "Matches a part of the name of any of the game's
                   [mechanics](https://boardgamegeek.com/browse/boardgamemechanic)"
     :example     "`mechanic:building` Matches any game with **Pattern Building**,
                   **Route/Network Building**, or **Deck / Pool Building** mechanics"}

    {:term        :publish
     :alias       :p
     :description "Matches a part of the name of any of the game's publishers"
     :example     "`publish:z-man` Matches any game published by **Z-Man Games**"}

    {:term        :design
     :alias       :desi
     :description "Matches a part of the name of any of the game's designers"
     :example     "`design:uwe` Matches any game designed by someone named **Uwe**"}

    {:term        :rating-votes
     :alias       :rv
     :description "Compares against the number of user ratings the game has received"
     :example     "`rating-votes>=1000` Matches any game with at least **1000** user ratings"}

    {:term        :average-rating
     :alias       :ar
     :description "Compares against the average user rating of a game"
     :example     "`average-rating<6.5` Matches any game with an average rating below **6.5**"}

    {:term        :geek-rating
     :alias       :gr
     :description "Compares against the game's BGG GeekRating metric"
     :example     "`geek-rating>=8.0` Matches any game with a GeekRating of at least **8.0**"}

    {:term        :rating-deviation
     :alias       :rd
     :description "Compares against the deviation in the game's ratings"
     :example     "`rating-deviation>=1.5` Matches against very divisive games"}

    {:term        :average-weight
     :alias       :aw
     :description "Compares against the game's average rated complexity"
     :example     "`average-weight>=3.7` Matches against games with an average weight of at least
                   **3.7**"}

    {:term        :weight-votes
     :alias       :wv
     :description "Compares against the game's number of complexity ratings"
     :example     "`weight-votes<1000` Matches any games that have less than **100** weight votes"}

    {:term        :year
     :description "Matches the year a game was released"
     :example     "`year=2018 family:\"roll-and-write\"` All the new Roll-and-Write games"}

    {:term        :age
     :description "Matches the game's minimum recommended player age"
     :example     "`age<=12` Matches any game that would be suitable for pre-teens"}

    {:term        :rec-players
     :alias       :rp
     :description "Matches against the number of players that are recommended by the BGG community"
     :example     "`rec-players=3` Matches any game that plays well with 3 players"}

    {:term        :best-players
     :alias       :bp
     :description "Matches against the number of players that are best for a game as determined by
                   the BGG community"
     :example     "`best-players=3` Matches any game that plays best with 3 players"}

    {:term        :quorum-players
     :alias       :qp
     :description "Matches against games that play well at a particular player count according to
                   feedback given by a quorum (currently 70%) of the players of said game. This
                   quorum includes both **best** and **recommended** player ratings"
     :example     "`quorum-players=3` Matches any game that plays well with 3 people as rated by at
                   least 4 out of 5 players"}

    {:term        :min-players
     :alias       :mnpr
     :description "Matches against the minimum number of required players"
     :example     "`min-players>=3` Matches any game that is playable with at least **3** players"}

    {:term        :max-players
     :alias       :mxpr
     :description "Matches against the maximum number of players supported by a game"
     :example     "`min-players=2 max-players=5` Matches any game that plays exactly **2** to **5**
                   players"}

    {:term        :min-playtime
     :alias       :mnpt
     :description "Matches against the minimum number of minutes needed to play a game"
     :example     "`min-playtime>=480` Matches **Twilight Imperium**"}

    {:term        :max-playtime
     :alias       :mxpt
     :description "Matches against the maximum number of minutes needed to play a game"
     :example     "`max-playtime<=30` Matches any game you can definitely complete in a half-hour"}

    {:term        :median-playtime
     :alias       :mdpt
     :description "Matches against the median number of minutes required to play a game, as sourced
                   by BGG user play data"
     :example     "`median-playtime<=60` Matches against any game that typically plays in an hour
                   or less"}

    {:term        :median-playtime-1
     :alias       :mdpt1
     :description "Matches against the median number of minutes required to play a game with 1
                   player, as sourced by BGG user play data"
     :example     "`median-playtime-1<=60` Matches against any game that typically plays in an hour
                   or less with 1 players"}

    {:term        :median-playtime-2
     :alias       :mdpt2
     :description "Matches against the median number of minutes required to play a game with 2
                   player, as sourced by BGG user play data"
     :example     "`median-playtime-1<=60` Matches against any game that typically plays in an hour
                   or less with 2 players"}

    {:term        :median-playtime-3
     :alias       :mdpt3
     :description "Matches against the median number of minutes required to play a game with 3
                   player, as sourced by BGG user play data"
     :example     "`median-playtime-1<=60` Matches against any game that typically plays in an hour
                   or less with 3 players"}

    {:term        :median-playtime-4
     :alias       :mdpt4
     :description "Matches against the median number of minutes required to play a game with 4
                   player, as sourced by BGG user play data"
     :example     "`median-playtime-1<=60` Matches against any game that typically plays in an hour
                   or less with 4 players"}

    {:term        :median-playtime-5
     :alias       :mdpt5
     :description "Matches against the median number of minutes required to play a game with 5
                   player, as sourced by BGG user play data"
     :example     "`median-playtime-1<=60` Matches against any game that typically plays in an hour
                   or less with 5 players"}

    {:term        :is:expansion
     :alias       :is:e
     :description "Only matches against expansions"
     :example     "- `is:expansion family:carcassonne` Expansions for Carcassonne
                   - `-is:expansion` Only standalone games"}

    {:term        :is:collection
     :alias       :is:c
     :description "Only matches against games which are collections of other games"
     :example     "`is:collection family:carcassonne` Show any games that collects other games in
                   the Carcassonne family"}

    {:term        :is:reimplementation
     :alias       :is:r
     :description "Games which are reimplementations of others"
     :example     "`-is:reimplementation -is:collection -is:expansion` Only shows standalone games
                   which are the original version"}

    {:term        :own
     :description "Games which are owned by a particular BGG user. Currently, you must manually
                   pull your BGG collection to initialize/update it. This must be done before you
                   use this term"
     :example     "`own:nafarlee bp=2` Only show games which are owned by **nafarlee** and are
                   **best with 2 players**"}})

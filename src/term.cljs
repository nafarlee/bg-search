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

    {:term :publish
     :alias :p}

    {:term :design
     :alias :desi}

    {:term :rating-votes
     :alias :rv}

    {:term :average-rating
     :alias :ar}

    {:term :geek-rating
     :alias :gr}

    {:term :rating-deviation
     :alias :rd}

    {:term :average-weight
     :alias :aw}

    {:term :weight-votes
     :alias :wv}

    {:term :year}

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

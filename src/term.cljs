(ns term
  (:require
    [markdown.core :refer [md->html]]))

(def terms
  #{{:term        :name
     :alias       :n
     :description (md->html "Matches a part of the games primary name")
     :example     (md->html "`name:catan` Games with **Catan** in the name")}

    {:term :art
     :alias :a}

    {:term :category
     :alias :c}

    {:term :desc}

    {:term :family
     :alias :f}

    {:term :mechanic
     :alias :m}

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

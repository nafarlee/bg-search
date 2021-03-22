const mirror = (o, s) => ({ [s]: s, ...o });

const tags = [
  'NAME',
  'ARTIST',
  'CATEGORY',
  'DESCRIPTION',
  'FAMILY',
  'MECHANIC',
  'PUBLISHER',
  'DESIGNER',
  'RATING_VOTES',
  'AVERAGE_RATING',
  'GEEK_RATING',
  'STEAMDB_RATING',
  'RATING_DEVIATION',
  'AVERAGE_WEIGHT',
  'WEIGHT_VOTES',
  'YEAR',
  'AGE',
  'MINIMUM_PLAYERS',
  'RECOMMENDED_PLAYERS',
  'BEST_PLAYERS',
  'MAXIMUM_PLAYERS',
  'QUORUM_PLAYERS',
  'MINIMUM_PLAYTIME',
  'MAXIMUM_PLAYTIME',
  'MEDIAN_PLAYTIME',
].reduce(mirror, {});

const operators = [
  '=',
  '>',
  '>=',
  '<',
  '<=',
].reduce(mirror, {});

const meta = [
  'EXPANSION',
  'COLLECTION',
  'REIMPLEMENTATION',
].reduce(mirror, {});

export default {
  operators,
  tags: {
    meta: {
      expansion: meta.EXPANSION,
      e: meta.EXPANSION,

      collection: meta.COLLECTION,
      c: meta.COLLECTION,

      reimplementation: meta.REIMPLEMENTATION,
      r: meta.REIMPLEMENTATION,
    },
    declarative: {
      name: tags.NAME,
      n: tags.NAME,

      art: tags.ARTIST,
      a: tags.ARTIST,

      category: tags.CATEGORY,
      c: tags.CATEGORY,

      desc: tags.DESCRIPTION,

      family: tags.FAMILY,
      f: tags.FAMILY,

      mechanic: tags.MECHANIC,
      m: tags.MECHANIC,

      publish: tags.PUBLISHER,
      p: tags.PUBLISHER,

      design: tags.DESIGNER,
      desi: tags.DESIGNER,
    },
    relational: {
      'rating-votes': tags.RATING_VOTES,
      rv: tags.RATING_VOTES,

      'average-rating': tags.AVERAGE_RATING,
      ar: tags.AVERAGE_RATING,

      'geek-rating': tags.GEEK_RATING,
      gr: tags.GEEK_RATING,

      'steamdb-rating': tags.STEAMDB_RATING,
      sr: tags.STEAMDB_RATING,

      'rating-deviation': tags.RATING_DEVIATION,
      rd: tags.RATING_DEVIATION,

      'average-weight': tags.AVERAGE_WEIGHT,
      aw: tags.AVERAGE_WEIGHT,

      'weight-votes': tags.WEIGHT_VOTES,
      wv: tags.WEIGHT_VOTES,

      year: tags.YEAR,
      age: tags.AGE,

      'rec-players': tags.RECOMMENDED_PLAYERS,
      rp: tags.RECOMMENDED_PLAYERS,

      'best-players': tags.BEST_PLAYERS,
      bp: tags.BEST_PLAYERS,

      'quorum-players': tags.QUORUM_PLAYERS,
      qp: tags.QUORUM_PLAYERS,

      'min-players': tags.MINIMUM_PLAYERS,
      mnpr: tags.MINIMUM_PLAYERS,

      'max-players': tags.MAXIMUM_PLAYERS,
      mxpr: tags.MAXIMUM_PLAYERS,

      'min-playtime': tags.MINIMUM_PLAYTIME,
      mnpt: tags.MINIMUM_PLAYTIME,

      'max-playtime': tags.MAXIMUM_PLAYTIME,
      mxpt: tags.MAXIMUM_PLAYTIME,

      'median-playtime': tags.MEDIAN_PLAYTIME,
      mdpt: tags.MEDIAN_PLAYTIME,
    },
  },
};

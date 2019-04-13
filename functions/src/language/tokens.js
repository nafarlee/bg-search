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
  'RATING_DEVIATION',
  'AVERAGE_WEIGHT',
  'WEIGHT_VOTES',
  'YEAR',
  'AGE',
  'MINIMUM_PLAYERS',
  'RECOMMENDED_PLAYERS',
  'BEST_PLAYERS',
  'MAXIMUM_PLAYERS',
  'MINIMUM_PLAYTIME',
  'MAXIMUM_PLAYTIME',
].reduce(mirror, {});


const operators = [
  'EQUAL',
  'GT',
  'GTE',
  'LT',
  'LTE',
].reduce(mirror, {});

const meta = [
  'EXPANSION',
  'COLLECTION',
  'REIMPLEMENTATION',
].reduce(mirror, {});

module.exports = {
  operators: {
    '=': operators.EQUAL,
    '>': operators.GT,
    '>=': operators.GTE,
    '<': operators.LT,
    '<=': operators.LTE,
  },
  tags: {
    meta: {
      expansion: meta.EXPANSION,
      collection: meta.COLLECTION,
      reimplementation: meta.REIMPLEMENTATION,
    },
    declarative: {
      name: tags.NAME,
      art: tags.ARTIST,
      category: tags.CATEGORY,
      desc: tags.DESCRIPTION,
      family: tags.FAMILY,
      mechanic: tags.MECHANIC,
      publish: tags.PUBLISHER,
      design: tags.DESIGNER,
    },
    relational: {
      'rating-votes': tags.RATING_VOTES,
      'average-rating': tags.AVERAGE_RATING,
      'geek-rating': tags.GEEK_RATING,
      'rating-deviation': tags.RATING_DEVIATION,
      'average-weight': tags.AVERAGE_WEIGHT,
      'weight-votes': tags.WEIGHT_VOTES,
      year: tags.YEAR,
      age: tags.AGE,
      'min-players': tags.MINIMUM_PLAYERS,
      'rec-players': tags.RECOMMENDED_PLAYERS,
      'best-players': tags.BEST_PLAYERS,
      'max-players': tags.MAXIMUM_PLAYERS,
      'min-playtime': tags.MINIMUM_PLAYTIME,
      'max-playtime': tags.MAXIMUM_PLAYTIME,
    },
  },
};

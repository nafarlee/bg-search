const _ = require('lodash');

const includes = (str, substr) => _.includes(_.toLower(str), _.toLower(substr));

const singleFieldSubstrings = _.mapValues({
  NAME: 'primary-name',
  DESCRIPTION: 'description',
}, field => (term, game) => includes(game[field], term.value));

const multipleFieldSubstrings = _.mapValues({
  ARTIST: 'artists',
  CATEGORY: 'categories',
  FAMILY: 'families',
  MECHANIC: 'mechanics',
  PUBLISHER: 'publishers',
  DESIGNER: 'designers',
}, field => (term, game) => _.some(game[field], i => includes(i, term.value)));

const operators = {
  EQUAL: (a, b) => a == b, // eslint-disable-line
  GT: (a, b) => a > b,
  GTE: (a, b) => a >= b,
  LT: (a, b) => a < b,
  LTE: (a, b) => a <= b,
};

const simpleRelationalComparisons = _.mapValues({
  RATING_VOTES: 'rating-votes',
  AVERAGE_RATING: 'average-rating',
  GEEK_RATING: 'bayes-rating',
  RATING_DEVIATION: 'rating-deviation',
  AVERAGE_WEIGHT: 'average-weight',
  WEIGHT_VOTES: 'weight-votes',
  YEAR: 'year',
  AGE: 'minimum-age',
  MINIMUM_PLAYERS: 'minimum-players',
  MAXIMUM_PLAYERS: 'maximum-players',
  MINIMUM_PLAYTIME: 'minimum-playtime',
  MAXIMUM_PLAYTIME: 'maximum-playtime',
}, field => (term, game) => operators[term.operator](game[field], term.value));

function RECOMMENDED_PLAYERS(term, game) {
  if (!game['community-recommended-players']) return false;
  const { counts } = game['community-recommended-players'];
  return _.some(counts, (votes, count) => {
    if (!operators[term.operator](count, term.value)) return false;
    const total = votes.best + votes.recommended + votes['not-recommended'];
    const recommended = votes.best + votes.recommended;
    return recommended / total > 0.5;
  });
}

function BEST_PLAYERS(term, game) {
  if (!game['community-recommended-players']) return false;
  const { counts } = game['community-recommended-players'];
  return _.some(counts, (votes, count) => {
    if (!operators[term.operator](count, term.value)) return false;
    const total = votes.best + votes.recommended + votes['not-recommended'];
    const recommended = votes.best;
    return recommended / total > 0.5;
  });
}

module.exports = _.mapValues({
  ...singleFieldSubstrings,
  ...multipleFieldSubstrings,
  ...simpleRelationalComparisons,
  BEST_PLAYERS,
  RECOMMENDED_PLAYERS,
}, fn => (term, game) => (
  term.negate ? !fn(term, game) : fn(term, game)
));

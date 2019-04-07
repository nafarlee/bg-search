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

function RATING_VOTES(term, game) {
  return operators[term.operator](term.value, game['rating-votes']);
}

function AVERAGE_RATING(term, game) {
  return operators[term.operator](term.value, game['averate-rating']);
}

function GEEK_RATING(term, game) {
  return operators[term.operator](term.value, game['bayes-rating']);
}

function RATING_DEVIATION(term, game) {
  return operators[term.operator](term.value, game['rating-deviation']);
}

function AVERAGE_WEIGHT(term, game) {
  return operators[term.operator](term.value, game['average-weight']);
}

module.exports = {
  ...singleFieldSubstrings,
  ...multipleFieldSubstrings,
  AVERAGE_RATING,
  AVERAGE_WEIGHT,
  GEEK_RATING,
  RATING_DEVIATION,
  RATING_VOTES,
};

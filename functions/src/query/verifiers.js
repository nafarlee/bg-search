const _ = require('lodash');

const includes = (str, substr) => _.includes(_.toLower(str), _.toLower(substr));

function NAME(term, game) {
  return includes(game['primary-name'], term.value);
}

function ARTIST(term, game) {
  return _.some(game.artists, a => includes(a, term.value));
}

function CATEGORY(term, game) {
  return _.some(game.categories, c => includes(c, term.value));
}

function DESCRIPTION(term, game) {
  return includes(game.description, term.value);
}

function FAMILY(term, game) {
  return _.some(game.families, f => includes(f, term.value));
}

function MECHANIC(term, game) {
  return _.some(game.mechanics, m => includes(m, term.value));
}

function PUBLISHER(term, game) {
  return _.some(game.publishers, p => includes(p, term.value));
}

function DESIGNER(term, game) {
  return _.some(game.designers, d => includes(d, term.value));
}

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
  ARTIST,
  AVERAGE_RATING,
  AVERAGE_WEIGHT,
  CATEGORY,
  DESCRIPTION,
  DESIGNER,
  FAMILY,
  GEEK_RATING,
  MECHANIC,
  PUBLISHER,
  RATING_DEVIATION,
  RATING_VOTES,
  NAME,
};

const _ = require('lodash');

const verifiers = require('./verifiers');

let verifyAND;
let verifyOR;

const baseVerifier = iterator => (terms, game) => (
  iterator(terms, (term) => {
    switch (term.type) {
      case 'AND': return verifyAND(term.terms, game);
      case 'OR': return verifyOR(term.terms, game);
      default: return verifiers[term.tag](term, game);
    }
  })
);

verifyAND = baseVerifier(_.every);
verifyOR = baseVerifier(_.some);

module.exports = verifyAND;

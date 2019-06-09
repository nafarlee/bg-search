const transpile = require('./lib');
const language = require('../language');

function fromOperator(op) {
  switch (op) {
    case 'GTE': return '>=';
    default:
      throw new Error('No operator conversion found');
  }
}

function toArgs(p) {
  switch (p.type) {
    case 'META': return [p.negate];
    case 'DECLARATIVE': return [p.value, p.negate];
    case 'RELATIONAL': return [fromOperator(p.operator), p.value, p.negate];
    default: throw new Error('Unkown predicate type');
  }
}

function query(s) {
  const predicates = language.tryParse(s);
  const result = predicates.map((p) => {
    const fn = transpile[p.tag];
    return fn(...toArgs(p));
  });

  const appension = result.map(r => r.text).join('\nINTERSECT\n');
  const pre = `SELECT DISTINCT primary_name, year
               FROM games`;
  const post = 'ORDER BY year';
  return `${pre} INTERSECT (${appension}) ${post};`;
}

module.exports = query;

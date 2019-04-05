const PS = require('parsimmon');

const tokens = require('./tokens');

module.exports = PS.createLanguage({
  Language(r) {
    return PS.alt(
      PS.seq(PS.optWhitespace, r.ExpressionList, PS.optWhitespace, PS.end)
        .map(([, exps]) => exps),
      PS.seq(PS.whitespace, PS.end).result([]),
      PS.end.result([]),
    );
  },

  ExpressionList(r) {
    return PS.alt(
      PS.seq(r.Expression, PS.whitespace, r.ExpressionList)
        .map(([exp,, exps]) => [exp, ...exps]),
      r.Expression
        .map(x => [x]),
    );
  },

  Expression(r) {
    return PS.alt(
      r.OrChain,
      r.SubExpression,
    );
  },

  OrChain(r) {
    return PS.seq(
      r.SubExpression,
      PS.whitespace,
      r.Or,
      PS.whitespace,
      r.SubExpression,
      PS.seq(PS.whitespace, r.Or, PS.whitespace, r.SubExpression).many(),
    ).map(([first,,,, second, rest]) => ({
      type: 'OR',
      terms: [
        first,
        second,
        ...rest.map(([,,, term]) => term),
      ],
    }));
  },

  SubExpression(r) {
    return PS.alt(
      r.Group,
      r.Term,
    );
  },

  Group(r) {
    return PS.seq(
      PS.string('('),
      PS.optWhitespace,
      r.ExpressionList,
      PS.optWhitespace,
      PS.string(')'),
    ).map(([,, terms]) => (
      terms.length === 1
        ? terms[0]
        : { type: 'AND', terms }
    ));
  },

  Term(r) {
    return PS.seq(
      PS.string('-').atMost(1),
      PS.alt(
        r.DeclarativeTerm,
        r.RelationalTerm,
      ),
    ).map(([[sign], term]) => ({ negate: sign === '-', ...term }));
  },

  DeclarativeTerm(r) {
    return PS.seq(
      r.DeclarativeTag,
      PS.string(':'),
      r.Value,
    ).map(([tag,, value]) => ({ type: 'DECLARATIVE', tag, value }));
  },

  RelationalTerm(r) {
    return PS.seq(
      r.RelationalTag,
      r.RelationalOperator,
      r.SimpleValue,
    ).map(([tag, operator, value]) => ({
      type: 'RELATIONAL',
      tag,
      operator,
      value,
    }));
  },

  Value(r) {
    return PS.alt(
      r.SimpleValue,
      r.QuotedValue,
    );
  },

  Or() {
    return PS.regexp(/or/i);
  },

  DeclarativeTag() {
    const pattern = Object.keys(tokens.tags.declarative).join('|');
    return PS.regexp(new RegExp(pattern, 'i'));
  },

  SimpleValue() {
    return PS.regexp(/[^") ]+/);
  },

  QuotedValue() {
    return PS.regexp(/"([^"]+)"/, 1);
  },

  RelationalTag() {
    const pattern = Object.keys(tokens.tags.relational).join('|');
    return PS.regexp(new RegExp(pattern, 'i'));
  },

  RelationalOperator() {
    const pattern = Object
      .keys(tokens.operators)
      .sort((a, b) => b.length - a.length)
      .join('|');
    return PS.regexp(new RegExp(pattern));
  },
}).Language;

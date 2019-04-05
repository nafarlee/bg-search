const PS = require('parsimmon');

module.exports = PS.createLanguage({
  Language(r) {
    return PS.alt(
      PS.seq(PS.optWhitespace, r.ExpressionList, PS.optWhitespace, PS.end),
      PS.seq(PS.whitespace, PS.end),
      PS.end,
    );
  },

  ExpressionList(r) {
    return PS.alt(
      PS.seq(r.Expression, PS.whitespace, r.ExpressionList),
      r.Expression,
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
    );
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
    return PS.alt(
      PS.string('name'),
      PS.string('art'),
      PS.string('category'),
      PS.string('desc'),
      PS.string('family'),
      PS.string('mechanic'),
      PS.string('publish'),
      PS.string('designer'),
    );
  },

  SimpleValue() {
    return PS.regexp(/[^) ]+/);
  },

  QuotedValue() {
    return PS.seq(
      PS.string('"'),
      PS.regexp(/[^"]+/),
      PS.string('"'),
    );
  },

  RelationalTag() {
    return PS.alt(
      PS.string('rating-votes'),
      PS.string('average-rating'),
      PS.string('geek-rating'),
      PS.string('rating-deviation'),
      PS.string('average-weight'),
      PS.string('weight-votes'),
      PS.string('year'),
      PS.string('age'),
      PS.string('min-players'),
      PS.string('rec-players'),
      PS.string('best-players'),
      PS.string('max-players'),
      PS.string('min-playtime'),
      PS.string('max-playtime'),
    );
  },

  RelationalOperator() {
    return PS.alt(
      PS.string('>='),
      PS.string('<='),
      PS.string('='),
      PS.string('>'),
      PS.string('<'),
    );
  },
}).Language;

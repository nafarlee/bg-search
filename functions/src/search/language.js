const PS = require('parsimmon');

module.exports = PS.createLanguage({
  Query(r) {
    return PS.alt(
      PS.seq(PS.optWhitespace, r.Term, PS.optWhitespace),
      PS.seq(
        PS.optWhitespace,
        r.Term,
        PS.whitespace,
        r.Query,
        PS.optWhitespace,
      ),
      PS.end,
    );
  },

  Term(r) {
    return PS.alt(
      PS.seq(r.DeclarativeTag, PS.string(':'), PS.alt(r.SimpleValue, r.QuotedValue)),
      PS.seq(r.RelationalTag, r.RelationalOperator, r.SimpleValue),
    );
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
    return PS.regexp(/[^ ]+/);
  },

  QuotedValue() {
    return PS.seq(
      PS.string('"'),
      PS.regexp(/[^" ]+/),
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
      PS.string('='),
      PS.string('>'),
      PS.string('>='),
      PS.string('<'),
      PS.string('<='),
    );
  },
}).Query;

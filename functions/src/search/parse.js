const PS = require('parsimmon');

const Language = PS.createLanguage({
  Query(r) {
    return r.Termlist;
  },

  Termlist(r) {
    return PS.alt(
      PS.seq(
        PS.optWhitespace,
        r.Term,
        PS.whitespace,
        r.Termlist,
        PS.optWhitespace,
      ),
      r.Term,
      PS.end,
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

  Term(r) {
    return PS.seq(
      r.Tag,
      r.Operator,
      r.Value,
    );
  },

  Value() {
    return PS.regexp(/[^" ]+/);
  },

  Operator() {
    return PS.alt(
      PS.string(':'),
      PS.string('='),
      PS.string('>'),
      PS.string('>='),
      PS.string('<'),
      PS.string('<='),
    );
  },
});

console.log(Language.Query.parse('name:cool name:no'));

const language = require('./language');

function randomSpace(max) {
  return ' '.repeat(Math.floor(Math.random() * max));
}

test('empty search', () => {
  language.tryParse('');
});

test('whitespace search', () => {
  language.tryParse(' ');
});

test('single declarative term searches', () => {
  [
    'name',
    'art',
    'category',
    'desc',
    'family',
    'mechanic',
    'publish',
    'designer',
  ].forEach((tag) => {
    language.tryParse(`${randomSpace(3)}${tag}:catan${randomSpace(3)}`);
  });
});

test('single relational term searches', () => {
  const tags = [
    'rating-votes',
    'average-rating',
    'geek-rating',
    'rating-deviation',
    'average-weight',
    'weight-votes',
    'year',
    'age',
    'min-players',
    'rec-players',
    'best-players',
    'max-players',
    'min-playtime',
    'max-playtime',
  ];
  const operators = [
    '=',
    '>',
    '>=',
    '<',
    '<=',
  ];
  tags.forEach((tag) => {
    operators.forEach((op) => {
      language.tryParse(`${randomSpace(3)}${tag}${op}1994${randomSpace(3)}`);
    });
  });
});

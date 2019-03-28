const language = require('./language');

test('empty search', () => {
  language.tryParse('');
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
    language.tryParse(`${tag}:catan`);
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
      language.tryParse(`${tag}${op}1994`);
    });
  });
});

const language = require('./language');

function randomSpace(max) {
  return ' '.repeat(Math.floor(Math.random() * max));
}

const declarativeTags = [
  'name',
  'art',
  'category',
  'desc',
  'family',
  'mechanic',
  'publish',
  'designer',
];

const relationalTags = [
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

const relationalOperators = [
  '=',
  '>',
  '>=',
  '<',
  '<=',
];

test('empty search', () => {
  language.tryParse('');
});

test('whitespace search', () => {
  language.tryParse(' ');
});

test('single declarative term searches', () => {
  declarativeTags.forEach((tag) => {
    language.tryParse(`${randomSpace(3)}${tag}:catan${randomSpace(3)}`);
  });
});

test('single relational term searches', () => {
  relationalTags.forEach((tag) => {
    relationalOperators.forEach((op) => {
      language.tryParse(`${randomSpace(3)}${tag}${op}1994${randomSpace(3)}`);
    });
  });
});

test('multiple declarative term searches', () => {
  const query = declarativeTags
    .map(tag => `${randomSpace(3)}${tag}:catan${randomSpace(3)}`)
    .join(' ');
  language.tryParse(query);
});

test('minimal or clause', () => {
  const query = [
    '',
    'name:catan',
    'or',
    'year=1994',
    '',
  ].join(randomSpace(3));
  language.tryParse(query);
});

test('lengthy or clause', () => {
  const query = [
    '',
    'name:catan',
    'or',
    'year=1994',
    'or',
    'year=1994',
    'or',
    'year=1994',
    '',
  ].join(randomSpace(3));
  language.tryParse(query);
});

test('grouping single term', () => {
  language.tryParse('(name:catan)');
});

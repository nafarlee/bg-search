const language = require('./language');

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

const spaces = ['', ' '];

test('empty search', () => {
  language.tryParse('');
});

test('whitespace search', () => {
  language.tryParse(' ');
});

test('single declarative term searches', () => {
  declarativeTags.forEach((tag) => {
    spaces.forEach((s) => {
      language.tryParse(`${s}${tag}:catan${s}`);
    });
  });
});

test('single relational term searches', () => {
  relationalTags.forEach((tag) => {
    relationalOperators.forEach((op) => {
      spaces.forEach((s) => {
        language.tryParse(`${s}${tag}${op}1994${s}`);
      });
    });
  });
});

test('multiple declarative term searches', () => {
  spaces.forEach((s) => {
    const query = declarativeTags
      .map(tag => `${s}${tag}:catan${s}`)
      .join(' ');
    language.tryParse(query);
  });
});

test('minimal or clause', () => {
  spaces.forEach((s) => {
    const query = [
      '',
      'name:catan',
      ' ',
      'or',
      ' ',
      'year=1994',
      '',
    ].join(s);
    language.tryParse(query);
  });
});

test('lengthy or clause', () => {
  spaces.forEach((s) => {
    const query = [
      '',
      'name:catan',
      ' ',
      'or',
      ' ',
      'year=1994',
      ' ',
      'or',
      ' ',
      'year=1994',
      ' ',
      'or',
      ' ',
      'year=1994',
      '',
    ].join(s);
    language.tryParse(query);
  });
});

test('grouping single term', () => {
  spaces.forEach((s) => {
    const query = [
      '(',
      'name:catan',
      ')',
    ].join(s);
    language.tryParse(query);
  });
});

test('grouping multiple terms', () => {
  spaces.forEach((s) => {
    const query = [
      '(',
      'year>1994',
      ' ',
      'name:catan',
      ')',
    ].join(s);
    language.tryParse(query);
  });
});

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
  expect(language.tryParse(''))
    .toEqual([]);
});

test('whitespace search', () => {
  expect(language.tryParse(' '))
    .toEqual([]);
});

test('single declarative term searches', () => {
  declarativeTags.forEach((tag) => {
    spaces.forEach((s) => {
      expect(language.tryParse(`${s}${tag}:catan${s}`))
        .toEqual([{
          negate: false,
          type: 'DECLARATIVE',
          value: 'catan',
          tag,
        }]);
    });
  });
});

test('single relational term searches', () => {
  relationalTags.forEach((tag) => {
    relationalOperators.forEach((op) => {
      spaces.forEach((s) => {
        expect(language.tryParse(`${s}${tag}${op}1994${s}`))
          .toEqual([{
            negate: false,
            type: 'RELATIONAL',
            value: '1994',
            operator: op,
            tag,
          }]);
      });
    });
  });
});

test('single negative declarative term searches', () => {
  declarativeTags.forEach((tag) => {
    spaces.forEach((s) => {
      expect(language.tryParse(`${s}-${tag}:catan${s}`))
        .toEqual([{
          negate: true,
          type: 'DECLARATIVE',
          value: 'catan',
          tag,
        }]);
    });
  });
});

test('multiple declarative term searches', () => {
  spaces.forEach((s) => {
    const query = declarativeTags
      .map(tag => `${s}${tag}:catan${s}`)
      .join(' ');
    const actual = language.tryParse(query);
    const expected = declarativeTags.map(tag => ({
      negate: false,
      type: 'DECLARATIVE',
      value: 'catan',
      tag,
    }));
    expect(actual).toEqual(expected);
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
    expect(language.tryParse(query))
      .toEqual([{
        type: 'OR',
        terms: [{
          tag: 'name',
          type: 'DECLARATIVE',
          value: 'catan',
          negate: false,
        }, {
          tag: 'year',
          type: 'RELATIONAL',
          value: '1994',
          operator: '=',
          negate: false,
        }],
      }]);
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
    expect(language.tryParse(query))
      .toEqual([{
        type: 'OR',
        terms: [{
          tag: 'name',
          type: 'DECLARATIVE',
          value: 'catan',
          negate: false,
        }, {
          tag: 'year',
          type: 'RELATIONAL',
          value: '1994',
          operator: '=',
          negate: false,
        }, {
          tag: 'year',
          type: 'RELATIONAL',
          value: '1994',
          operator: '=',
          negate: false,
        }, {
          tag: 'year',
          type: 'RELATIONAL',
          value: '1994',
          operator: '=',
          negate: false,
        }],
      }]);
  });
});

test('grouping single term', () => {
  spaces.forEach((s) => {
    const query = [
      '(',
      'name:catan',
      ')',
    ].join(s);
    expect(language.tryParse(query))
      .toEqual([{
        type: 'DECLARATIVE',
        tag: 'name',
        value: 'catan',
        negate: false,
      }]);
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
    expect(language.tryParse(query))
      .toEqual([{
        type: 'AND',
        terms: [{
          tag: 'year',
          type: 'RELATIONAL',
          value: '1994',
          operator: '>',
          negate: false,
        }, {
          tag: 'name',
          type: 'DECLARATIVE',
          value: 'catan',
          negate: false,
        }],
      }]);
  });
});

test('complete test', () => {
  spaces.forEach((s) => {
    const query = [
      '',
      'rating-votes>=1000',
      ' ',
      'name:catan',
      ' ',
      'or',
      ' ',
      '(',
      'year>=1993',
      ')',
      ' ',
      'or',
      ' ',
      '(',
      'mechanic:dice',
      ' ',
      'age>4',
      ' ',
      'or',
      ' ',
      '-age<4',
      ' ',
      ')',
      '',
    ].join(s);
    expect(language.tryParse(query))
      .toEqual([{
        tag: 'rating-votes',
        value: '1000',
        operator: '>=',
        negate: false,
        type: 'RELATIONAL',
      }, {
        type: 'OR',
        terms: [{
          tag: 'name',
          type: 'DECLARATIVE',
          value: 'catan',
          negate: false,
        }, {
          tag: 'year',
          operator: '>=',
          value: '1993',
          type: 'RELATIONAL',
          negate: false,
        }, {
          type: 'AND',
          terms: [{
            tag: 'mechanic',
            type: 'DECLARATIVE',
            value: 'dice',
            negate: false,
          }, {
            type: 'OR',
            terms: [{
              tag: 'age',
              operator: '>',
              value: '4',
              negate: false,
              type: 'RELATIONAL',
            }, {
              tag: 'age',
              operator: '<',
              value: '4',
              negate: true,
              type: 'RELATIONAL',
            }],
          }],
        }],
      }]);
  });
});

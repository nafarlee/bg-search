import language from './index';
import tokens from './tokens';

const spaces = ['', ' '];

test('empty search', () => {
  expect(language.tryParse(''))
    .toEqual([]);
});

test('whitespace search', () => {
  expect(language.tryParse(' '))
    .toEqual([]);
});

test('quoted term', () => {
  expect(language.tryParse('name:"7 wonders"'))
    .toEqual([{
      tag: tokens.tags.declarative.name,
      value: '7 wonders',
      type: 'DECLARATIVE',
      negate: false,
    }]);
});

test('single declarative term searches', () => {
  Object.keys(tokens.tags.declarative).forEach((tag) => {
    spaces.forEach((s) => {
      expect(language.tryParse(`${s}${tag}:catan${s}`)).toEqual([{
        negate: false,
        type: 'DECLARATIVE',
        value: 'catan',
        tag: tokens.tags.declarative[tag],
      }]);
      expect(language.tryParse(`${s}${tag.toUpperCase()}:catan${s}`)).toEqual([{
        negate: false,
        type: 'DECLARATIVE',
        value: 'catan',
        tag: tokens.tags.declarative[tag],
      }]);
    });
  });
});

test('single relational term searches', () => {
  Object.keys(tokens.tags.relational).forEach((tag) => {
    Object.keys(tokens.operators).forEach((op) => {
      spaces.forEach((s) => {
        expect(language.tryParse(`${s}${tag}${op}1994${s}`)).toEqual([{
          negate: false,
          type: 'RELATIONAL',
          value: '1994',
          operator: tokens.operators[op],
          tag: tokens.tags.relational[tag],
        }]);
        expect(language.tryParse(`${s}${tag.toUpperCase()}${op}1994${s}`))
          .toEqual([{
            negate: false,
            type: 'RELATIONAL',
            value: '1994',
            operator: tokens.operators[op],
            tag: tokens.tags.relational[tag],
          }]);
      });
    });
  });
});

test('single meta term searches', () => {
  Object.keys(tokens.tags.meta).forEach((value) => {
    spaces.forEach((s) => {
      expect(language.tryParse(`${s}is:${value}${s}`)).toEqual([{
        type: 'META',
        tag: tokens.tags.meta[value],
        negate: false,
      }]);
      expect(language.tryParse(`${s}IS:${value}${s}`)).toEqual([{
        type: 'META',
        tag: tokens.tags.meta[value],
        negate: false,
      }]);
    });
  });
});

test('single negative declarative term searches', () => {
  Object.keys(tokens.tags.declarative).forEach((tag) => {
    spaces.forEach((s) => {
      expect(language.tryParse(`${s}-${tag}:catan${s}`))
        .toEqual([{
          negate: true,
          type: 'DECLARATIVE',
          value: 'catan',
          tag: tokens.tags.declarative[tag],
        }]);
    });
  });
});

test('multiple declarative term searches', () => {
  spaces.forEach((s) => {
    const query = Object.keys(tokens.tags.declarative)
      .map((tag) => `${s}${tag}:catan${s}`)
      .join(' ');
    const actual = language.tryParse(query);
    const expected = Object.keys(tokens.tags.declarative).map((tag) => ({
      negate: false,
      type: 'DECLARATIVE',
      value: 'catan',
      tag: tokens.tags.declarative[tag],
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
          tag: tokens.tags.declarative.name,
          type: 'DECLARATIVE',
          value: 'catan',
          negate: false,
        }, {
          tag: tokens.tags.relational.year,
          type: 'RELATIONAL',
          value: '1994',
          operator: tokens.operators['='],
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
          tag: tokens.tags.declarative.name,
          type: 'DECLARATIVE',
          value: 'catan',
          negate: false,
        }, {
          tag: tokens.tags.relational.year,
          type: 'RELATIONAL',
          value: '1994',
          operator: tokens.operators['='],
          negate: false,
        }, {
          tag: tokens.tags.relational.year,
          type: 'RELATIONAL',
          value: '1994',
          operator: tokens.operators['='],
          negate: false,
        }, {
          tag: tokens.tags.relational.year,
          type: 'RELATIONAL',
          value: '1994',
          operator: tokens.operators['='],
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
        tag: tokens.tags.declarative.name,
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
          tag: tokens.tags.relational.year,
          type: 'RELATIONAL',
          value: '1994',
          operator: tokens.operators['>'],
          negate: false,
        }, {
          tag: tokens.tags.declarative.name,
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
        tag: tokens.tags.relational['rating-votes'],
        value: '1000',
        operator: tokens.operators['>='],
        negate: false,
        type: 'RELATIONAL',
      }, {
        type: 'OR',
        terms: [{
          tag: tokens.tags.declarative.name,
          type: 'DECLARATIVE',
          value: 'catan',
          negate: false,
        }, {
          tag: tokens.tags.relational.year,
          operator: tokens.operators['>='],
          value: '1993',
          type: 'RELATIONAL',
          negate: false,
        }, {
          type: 'AND',
          terms: [{
            tag: tokens.tags.declarative.mechanic,
            type: 'DECLARATIVE',
            value: 'dice',
            negate: false,
          }, {
            type: 'OR',
            terms: [{
              tag: tokens.tags.relational.age,
              operator: tokens.operators['>'],
              value: '4',
              negate: false,
              type: 'RELATIONAL',
            }, {
              tag: tokens.tags.relational.age,
              operator: tokens.operators['<'],
              value: '4',
              negate: true,
              type: 'RELATIONAL',
            }],
          }],
        }],
      }]);
  });
});

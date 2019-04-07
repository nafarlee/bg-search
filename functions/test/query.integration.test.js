const language = require('../src/language');
const verify = require('../src/query');
const catan = require('./catan');

test('match', () => {
  const input = '(name:nonsense rating-votes=0 design:me) or (name:catan rating-votes>=10000 design:"Klaus Teuber" -max-playtime>140)';
  const tree = language.tryParse(input);
  expect(verify(tree, catan)).toBe(true);
});

test('match best-players', () => {
  const input = 'best-players=4';
  expect(verify(language.tryParse(input), catan)).toBe(true);
});

test('match rec-players', () => {
  const input = 'rec-players>2';
  expect(verify(language.tryParse(input), catan)).toBe(true);
});

test('match -is:expansion', () => {
  const input = '-is:expansion';
  expect(verify(language.tryParse(input), catan)).toBe(true);
});

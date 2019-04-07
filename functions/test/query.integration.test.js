const language = require('../src/language');
const verify = require('../src/query');
const catan = require('./catan');

test('match', () => {
  const input = '(name:nonsense rating-votes=0 design:me) or (name:catan rating-votes>=10000 design:"Klaus Teuber" -max-playtime>140)';
  const tree = language.tryParse(input);
  expect(verify(tree, catan)).toBe(true);
});

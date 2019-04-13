const { search } = require('./views');

test('no game results', () => {
  const req = {
    protocol: 'https',
    originalURL: '/search',
    hostname: 'example.com',
  };
  expect(search({ req, games: [] }))
    .toBe('<!DOCTYPE html>\n<h1>No more results!</h1>');
});

test('first results page', () => {
  const req = {
    protocol: 'https',
    originalURL: '/search?query=name%3Acatan&order=bayes-rating&direction=desc',
    hostname: 'example.com',
  };
  const games = [{
    'primary-name': 'Catan',
    id: 1,
    year: 1995,
  }];
  expect(search({ req, games }))
    .toBe(`
<!DOCTYPE html>
<h1><a href="https://boardgamegeek.com/boardgame/1">Catan (1995)</a></h1>
<br>
<p><a href="https://example.com/search?query=name%3Acatan&order=bayes-rating&direction=desc&checkpoint=1">Next</a></p>`);
});

test('second results page', () => {
  const req = {
    protocol: 'https',
    originalURL: '/search?query=name%3Acatan&order=bayes-rating&checkpoint=42&direction=desc',
    hostname: 'example.com',
  };
  const games = [{
    'primary-name': 'Catan',
    id: 1,
    year: 1995,
  }];
  expect(search({ req, games }))
    .toBe(`
<!DOCTYPE html>
<h1><a href="https://boardgamegeek.com/boardgame/1">Catan (1995)</a></h1>
<br>
<p><a href="https://example.com/search?query=name%3Acatan&order=bayes-rating&checkpoint=1&direction=desc">Next</a></p>`);
});

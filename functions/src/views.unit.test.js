const { search } = require('./views');

test('single game', () => {
  const actual = search({
    req: {
      originalUrl: '?query=&order=bayes_rating&direction=DESC',
      protocol: 'https',
      hostname: 'us-central1-bg-search.cloudfunctions.net',
      query: {
        offset: 0,
      },
    },
    games: [{
      primary_name: 'Senet',
      id: 2399,
      year: -3500,
    }],
    fnName: 'search',
  });
  const expected = `
    <!DOCTYPE html>
    <h2><a href="https://boardgamegeek.com/boardgame/2399">Senet (-3500)</a></h2>
    <br>
    <p><a href="https://us-central1-bg-search.cloudfunctions.net?query=&order=bayes_rating&direction=DESC&offset=1">Next</a></p>
  `;
  expect(actual).toEqual(expected);
});

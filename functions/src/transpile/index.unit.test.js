const transpile = require('./index');

expect.extend({
  toMatchIgnoringWhitespace(received, other) {
    const regex = /\s+/g;
    const replacement = ' ';
    const expected = received.trim().replace(regex, replacement);
    const actual = other.trim().replace(regex, replacement);
    return {
      message: () => `Expected: ${expected}\nReceived: ${actual}`,
      pass: expected === actual,
    };
  },
});

test('empty input', () => {
  const input = '';
  const { text, values } = transpile(input, 'bayes_rating', 'DESC', 0);
  expect(text).toMatchIgnoringWhitespace(`
    SELECT primary_name, rating_votes, average_rating, bayes_rating, rating_deviation, average_weight, weight_votes, year, minimum_age, minimum_players, maximum_players, minimum_playtime, maximum_playtime, description
              FROM games
              ORDER BY bayes_rating DESC
              LIMIT 25 OFFSET $1
  `);
  expect(values).toEqual([0]);
});

test("a cooperative dice game that isn't a collection with at least 500 ratings", () => {
  const input = '-is:expansion -is:collection rating-votes>=500 mechanic:coop mechanic:dice';
  const { text, values } = transpile(input, 'bayes_rating', 'DESC', 0);
  expect(text).toMatchIgnoringWhitespace(`
    SELECT primary_name, rating_votes, average_rating, bayes_rating, rating_deviation, average_weight, weight_votes, year, minimum_age, minimum_players, maximum_players, minimum_playtime, maximum_playtime, description
              FROM games
              INTERSECT
              SELECT primary_name, rating_votes, average_rating, bayes_rating, rating_deviation, average_weight, weight_votes, year, minimum_age, minimum_players, maximum_players, minimum_playtime, maximum_playtime, description
               FROM games
               LEFT JOIN expansions
                 ON id = expansion
               WHERE base IS  NULL INTERSECT SELECT primary_name, rating_votes, average_rating, bayes_rating, rating_deviation, average_weight, weight_votes, year, minimum_age, minimum_players, maximum_players, minimum_playtime, maximum_playtime, description
               FROM games
               LEFT JOIN collections
                 ON id = collection
               WHERE item IS  NULL INTERSECT SELECT primary_name, rating_votes, average_rating, bayes_rating, rating_deviation, average_weight, weight_votes, year, minimum_age, minimum_players, maximum_players, minimum_playtime, maximum_playtime, description
             FROM games
             WHERE  rating_votes >= $1  INTERSECT SELECT DISTINCT primary_name, rating_votes, average_rating, bayes_rating, rating_deviation, average_weight, weight_votes, year, minimum_age, minimum_players, maximum_players, minimum_playtime, maximum_playtime, description
             FROM games a, games_mechanics ab, mechanics b
             WHERE a.id = ab.game_id
               AND ab.mechanic_id = b.id
               AND mechanic ~~* $2 INTERSECT SELECT DISTINCT primary_name, rating_votes, average_rating, bayes_rating, rating_deviation, average_weight, weight_votes, year, minimum_age, minimum_players, maximum_players, minimum_playtime, maximum_playtime, description
             FROM games a, games_mechanics ab, mechanics b
             WHERE a.id = ab.game_id
               AND ab.mechanic_id = b.id
               AND mechanic ~~* $3
              ORDER BY bayes_rating DESC
              LIMIT 25 OFFSET $4
  `);
  expect(values).toEqual(['500', '%coop%', '%dice%', 0]);
});

test('a worker placement game that is best with 2 or 3 players', () => {
  const input = 'mechanic:worker best-players=2 or best-players=3';
  const { text, values } = transpile(input, 'bayes_rating', 'DESC', 0);
  expect(text).toMatchIgnoringWhitespace(`
    SELECT primary_name, rating_votes, average_rating, bayes_rating, rating_deviation, average_weight, weight_votes, year, minimum_age, minimum_players, maximum_players, minimum_playtime, maximum_playtime, description
              FROM games
              INTERSECT
              (SELECT DISTINCT primary_name, rating_votes, average_rating, bayes_rating, rating_deviation, average_weight, weight_votes, year, minimum_age, minimum_players, maximum_players, minimum_playtime, maximum_playtime, description
             FROM games a, games_mechanics ab, mechanics b
             WHERE a.id = ab.game_id
               AND ab.mechanic_id = b.id
               AND mechanic ~~* $1 INTERSECT SELECT primary_name, rating_votes, average_rating, bayes_rating, rating_deviation, average_weight, weight_votes, year, minimum_age, minimum_players, maximum_players, minimum_playtime, maximum_playtime, description
               FROM games g, player_recommendations pr
               WHERE g.id = pr.id
                 AND players && $2::int4range
                 AND  best > (recommended + not_recommended) UNION SELECT primary_name, rating_votes, average_rating, bayes_rating, rating_deviation, average_weight, weight_votes, year, minimum_age, minimum_players, maximum_players, minimum_playtime, maximum_playtime, description
               FROM games g, player_recommendations pr
               WHERE g.id = pr.id
                 AND players && $3::int4range
                 AND  best > (recommended + not_recommended))
              ORDER BY bayes_rating DESC
              LIMIT 25 OFFSET $4
  `);
  expect(values).toEqual(['%worker%', '[2,2]', '[3,3]', 0]);
});

import transpile from './index';

expect.extend({
  toMatchIgnoringWhitespace(received, other) {
    const regex = /\s+/g;
    const replacement = ' ';
    const expected = received.trim().replace(regex, replacement);
    const actual = other.trim().replace(regex, replacement);
    expect(expected).toEqual(actual);
    return { pass: true };
  },
});

test('empty input', () => {
  const input = '';
  const { text, values } = transpile(input, 'bayes_rating', 'DESC', 0);
  expect(text).toMatchIgnoringWhitespace(`
    SELECT DISTINCT
      id,
      primary_name,
      rating_votes,
      average_rating,
      bayes_rating,
      rating_deviation,
      average_weight,
      weight_votes,
      year,
      minimum_age,
      minimum_players,
      maximum_players,
      minimum_playtime,
      maximum_playtime,
      description
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
    SELECT DISTINCT
      id,
      primary_name,
      rating_votes,
      average_rating,
      bayes_rating,
      rating_deviation,
      average_weight,
      weight_votes,
      year,
      minimum_age,
      minimum_players,
      maximum_players,
      minimum_playtime,
      maximum_playtime,
      description
    FROM (SELECT id
          FROM games
          LEFT JOIN expansions
            ON id = expansion
          WHERE base IS NULL
        INTERSECT ALL
          SELECT id
          FROM games
          LEFT JOIN collections
            ON id = collection
          WHERE item IS NULL
        INTERSECT ALL
          SELECT id
          FROM games
          WHERE rating_votes >= $1
        INTERSECT ALL
          SELECT a.id
          FROM games a, games_mechanics ab, mechanics b
          WHERE a.id = ab.game_id
            AND ab.mechanic_id = b.id
          GROUP BY a.id
          HAVING BOOL_OR(mechanic ~~* $2) != false
        INTERSECT ALL
          SELECT a.id
          FROM games a, games_mechanics ab, mechanics b
          WHERE a.id = ab.game_id
            AND ab.mechanic_id = b.id
          GROUP BY a.id
          HAVING BOOL_OR(mechanic ~~* $3) != false) AS GameSubquery
    NATURAL INNER JOIN games
    ORDER BY bayes_rating DESC
    LIMIT 25 OFFSET $4
  `);
  expect(values).toEqual(['500', '%coop%', '%dice%', 0]);
});

test('a worker placement game that is best with 2 or 3 players', () => {
  const input = 'mechanic:worker best-players=2 or best-players=3';
  const { text, values } = transpile(input, 'bayes_rating', 'DESC', 0);
  expect(text).toMatchIgnoringWhitespace(`
    SELECT DISTINCT
      id,
      primary_name,
      rating_votes,
      average_rating,
      bayes_rating,
      rating_deviation,
      average_weight,
      weight_votes,
      year,
      minimum_age,
      minimum_players,
      maximum_players,
      minimum_playtime,
      maximum_playtime,
      description
    FROM (SELECT a.id
          FROM games a, games_mechanics ab, mechanics b
          WHERE a.id = ab.game_id
            AND ab.mechanic_id = b.id
          GROUP BY a.id
          HAVING BOOL_OR(mechanic ~~* $1) != false
          INTERSECT ALL (SELECT a.id
                         FROM games a, player_recommendations b
                         WHERE a.id = b.id
                           AND players && $2::int4range
                           AND  best > (recommended + not_recommended)
                         UNION ALL
                         SELECT a.id
                         FROM games a, player_recommendations b
                         WHERE a.id = b.id
                           AND players && $3::int4range
                           AND best > (recommended + not_recommended)))
    AS GameSubquery
    NATURAL INNER JOIN games
    ORDER BY bayes_rating DESC
    LIMIT 25 OFFSET $4
  `);
  expect(values).toEqual(['%worker%', '[2,2]', '[3,3]', 0]);
});

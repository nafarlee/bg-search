#!/usr/bin/env node
const _ = require('lodash');

function toSQL(table, columns, uniques, chunks) {
  const values = _.flatten(chunks);
  const positions = _(values)
    .map((_v, i) => `$${i + 1}`)
    .chunk(columns.length)
    .map(chunk => `(${chunk.join(', ')})`)
    .join(', ');

  const joined = columns.join(', ');
  const sets = columns
    .map(c => `${c} = EXCLUDED.${c}`)
    .join(', ');

  return [
    `INSERT INTO ${table} (${joined})
     VALUES ${positions}
     ON CONFLICT (${uniques.join(', ')})
     DO UPDATE SET ${sets};`,
    values,
  ];
}

function flattenWithID(games, prop) {
  return _.flatMap(
    games,
    g => _.map(
      g[prop],
      p => ({ id: p.id, gameID: g.id }),
    ),
  );
}

const kvInsert = (table, columns, prop = table) => (games) => {
  const props = _
    .chain(games)
    .flatMap(prop)
    .compact()
    .uniqWith(_.isEqual)
    .value();

  if (_.isEmpty(props)) return null;

  return toSQL(
    table,
    columns,
    columns.slice(0, 1),
    props.map(p => [p.id, p.value]),
  );
};

const junctionInsert = (table, columns, prop = table) => (games) => {
  const props = flattenWithID(games, prop);
  if (_.isEmpty(props)) return null;

  return toSQL(
    table,
    columns,
    columns,
    props.map(({ id, gameID }) => [gameID, id]),
  );
};

const soloJunctionInsert = (table, columns, props) => (games) => {
  let leftToRight = flattenWithID(games, props[0]);
  let rightToLeft = flattenWithID(games, props[1]);
  if (_.every([leftToRight, rightToLeft], _.isEmpty)) {
    return null;
  }

  leftToRight = leftToRight || [];
  rightToLeft = rightToLeft || [];


  return toSQL(
    table,
    columns,
    columns,
    _.concat(
      rightToLeft.map(({ id, gameID }) => [id, gameID]),
      leftToRight.map(({ id, gameID }) => [gameID, id]),
    ),
  );
};

const tables = {
  games(games) {
    const mapped = _.map(
      games,
      g => ({
        id: g.id,
        image: g.image,
        thumbnail: g.thumbnail,
        average_rating: g['average-rating'],
        average_weight: g['average-weight'],
        bayes_rating: g['bayes-rating'],
        description: g.description,
        maximum_players: g['maximum-players'],
        maximum_playtime: g['maximum-playtime'],
        minimum_age: g['minimum-age'],
        minimum_players: g['minimum-players'],
        minimum_playtime: g['minimum-playtime'],
        primary_name: g['primary-name'],
        rating_deviation: g['rating-deviation'],
        rating_votes: g['rating-votes'],
        weight_votes: g['weight-votes'],
        year: g.year,
      }),
    );

    const columns = _.keys(mapped[0]);
    const values = _.map(mapped, m => _.map(columns, c => m[c]));

    return toSQL('games', columns, ['id'], values);
  },

  alternate_names(games) {
    const chunks = _.flatMap(
      games,
      g => _.map(
        g['alternate-names'],
        name => [g.id, name],
      ),
    );
    if (_.isEmpty(chunks)) return null;

    const columns = ['id', 'alternate_name'];
    return toSQL(
      'alternate_names',
      columns,
      columns,
      chunks,
    );
  },

  reimplementations: soloJunctionInsert(
    'reimplementations',
    ['original', 'reimplementation'],
    ['reimplemented-by', 'reimplements'],
  ),

  collections: soloJunctionInsert(
    'collections',
    ['item', 'collection'],
    ['contained-in', 'contains'],
  ),

  expansions: soloJunctionInsert(
    'expansions',
    ['base', 'expansion'],
    ['expanded-by', 'expands'],
  ),

  publishers: kvInsert('publishers', ['id', 'publisher']),
  games_publishers: junctionInsert(
    'games_publishers',
    ['game_id', 'publisher_id'],
    'publishers',
  ),

  mechanics: kvInsert('mechanics', ['id', 'mechanic']),
  games_mechanics: junctionInsert(
    'games_mechanics',
    ['game_id', 'mechanic_id'],
    'mechanics',
  ),

  families: kvInsert('families', ['id', 'family']),
  games_families: junctionInsert(
    'games_families',
    ['game_id', 'family_id'],
    'families',
  ),

  artists: kvInsert('artists', ['id', 'artist']),
  games_artists: junctionInsert(
    'games_artists',
    ['game_id', 'artist_id'],
    'artists',
  ),

  categories: kvInsert('categories', ['id', 'category']),
  games_categories: junctionInsert(
    'games_categories',
    ['game_id', 'category_id'],
    'categories',
  ),

  designers: kvInsert('designers', ['id', 'designer']),
  games_designers: junctionInsert(
    'games_designers',
    ['game_id', 'designer_id'],
    'designers',
  ),
};

function insert(games) {
  return _(tables)
    .map(f => f(games))
    .compact()
    .value();
}

module.exports = insert;

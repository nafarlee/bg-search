#!/usr/bin/env node
const { promisify } = require('util');

const { Client } = require('pg');
const parseString = promisify(require('xml2js').parseString);

const get = require('../src/get');
const marshall = require('../src/marshall');

const baseURL = 'https://api.geekdo.com/xmlapi2/things';

(async () => {
  const client = new Client({
    user: 'postgres',
    database: 'postgres',
  });
  const game = await get(`${baseURL}?id=174430&stats=1&type=boardgame,boardgameexpansion`)
    .then(parseString)
    .then(body => marshall(body.items.item[0]));
  await client.connect();
  await client.query('BEGIN');
  const res = await client.query(
    `INSERT INTO games
     VALUES (
       $1,
       $2,
       $3,
       DEFAULT,
       $4,
       $5,
       $6,
       $7,
       $8,
       $9,
       $10,
       $11,
       $12,
       $13,
       $14,
       $15,
       $16,
       $17
     );`,
    [
      game.id,
      game.image,
      game.thumbnail,
      game['average-rating'],
      game['average-weight'],
      game['bayes-rating'],
      game.description,
      game['maximum-players'],
      game['maximum-playtime'],
      game['minimum-age'],
      game['minimum-players'],
      game['minimum-playtime'],
      game['primary-name'],
      game['rating-deviation'],
      game['rating-votes'],
      game['weight-votes'],
      game.year,
    ],
  );
  await client.query('COMMIT');
  console.log(res.rows);
  await client.end();
})();

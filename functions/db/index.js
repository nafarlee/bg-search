#!/usr/bin/env node
const { promisify } = require('util');

const { Client } = require('pg');
const parseString = promisify(require('xml2js').parseString);
const _ = require('lodash');

const get = require('../src/get');
const marshall = require('../src/marshall');
const insert = require('./insert');

const baseURL = 'https://api.geekdo.com/xmlapi2/things';

(async () => {
  const client = new Client({
    user: 'postgres',
    database: 'postgres',
  });
  const game = await get(`${baseURL}?id=${_.random(1, 500)}&stats=1&type=boardgame,boardgameexpansion`)
    .then(parseString)
    .then(body => marshall(body.items.item[0]));
  await client.connect();

  try {
    await client.query('BEGIN');
    console.log(insert(game));
    for (const query of insert(game)) {
      await client.query(...query);
    }
    await client.query('COMMIT');
  } catch (e) {
    await client.query('ROLLBACK');
  } finally {
    await client.end();
  }
})();

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
  const id = _.random(1, 500);
  console.log('ID: ', id);
  const body = await get(`${baseURL}?id=${id}&stats=1&type=boardgame,boardgameexpansion`)
    .then(parseString);

  if (!body.items.item) return;

  const game = marshall(body.items.item[0]);

  await client.connect();
  try {
    await client.query('BEGIN');
    const queries = insert(game);
    for (const query of queries) {
      console.log(query);
      await client.query(...query);
    }
    await client.query('COMMIT');
  } catch (e) {
    console.error(e);
    await client.query('ROLLBACK');
  } finally {
    await client.end();
  }
})();

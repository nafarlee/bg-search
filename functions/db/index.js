#!/usr/bin/env node
const { promisify } = require('util');

const { Client } = require('pg');
const parseString = promisify(require('xml2js').parseString);

const get = require('../src/get');
const marshall = require('../src/marshall');
const insert = require('./insert');

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
  const res = await client.query(...insert(game));
  await client.query('COMMIT');
  console.log(res.rows);
  await client.end();
})();

#!/usr/bin/env node
const { Client } = require('pg');

const { NAME } = require('./transpile');

(async () => {
  const client = new Client({
    user: 'postgres',
    database: 'postgres',
  });
  await client.connect();
  const query = NAME('at');
  const res = await client.query(query.text, query.values);

  console.log(res.rows);

  await client.end();
})();

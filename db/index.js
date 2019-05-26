const { Client } = require('pg');

const { name } = require('./transpile');

(async () => {
  const client = new Client({
    user: 'postgres',
    database: 'postgres',
  });
  await client.connect();
  const query = name('catan');
  const res = await client.query(query.text, query.values);

  console.log(res.rows);

  await client.end();
})();

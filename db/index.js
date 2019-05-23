const { Client } = require('pg');

(async () => {
  const client = new Client({
    user: 'postgres',
    database: 'postgres',
  });
  await client.connect();
  const res = await client.query('SELECT * from games');

  console.log(res.rows[0]);

  await client.end();
})();

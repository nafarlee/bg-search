const { Client } = require('pg');

const transpile = require('./index');

let client;

beforeAll(() => {
  client = new Client({
    user: 'postgres',
    database: 'postgres',
  });
  return client.connect();
});

afterAll(() => {
  client.end();
});

test("a cooperative dice game that isn't a collection with at least 500 ratings", () => {
  const input = '-is:expansion -is:collection rating-votes>=500 mechanic:coop mechanic:dice';
  return client.query(transpile(input));
});

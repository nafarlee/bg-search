import { promisify } from 'util';

import { Client } from 'pg';
import { range } from 'lodash';
import xml2js from 'xml2js';

import get from '../get';
import marshall from '../marshall/index';
import { insert } from '../db/insert';
import credentials from '../db-credentials.json';

const parseString = promisify(xml2js.parseString);
const baseURL = 'https://api.geekdo.com/xmlapi2/things';

export default async function pull(req, res) {
  const client = new Client(credentials);

  await client.connect();
  const { rows: [{ count }] } = await client.query('SELECT count FROM globals');

  const newCount = count + 500;
  const IDs = range(count, newCount);
  const xml = await get(`${baseURL}?stats=1&type=boardgame,boardgameexpansion&id=${IDs}`);
  const body = await parseString(xml);

  if (!body.items.item) {
    await client.query('UPDATE globals SET count = $1 WHERE id = $2', [1, 1]);
    console.log('SUCCESS: Mobius Strip');
    return res.status(200).send();
  }

  await client.query('BEGIN');
  await client.query('UPDATE globals SET count = $1 WHERE id = $2', [newCount, 1]);
  try {
    const games = body.items.item.map(marshall);
    await Promise.all(insert(games).map((q) => client.query(...q)));
    await client.query('COMMIT');
    console.log(`SUCCESS: ${count}..${newCount - 1}`);
    return res.status(200).send();
  } catch (err) {
    console.error(err);
    console.error(`ERROR: ${count}..${newCount - 1}`);
    await client.query('ROLLBACK');
    return res.status(500).send();
  } finally {
    await client.end();
  }
}

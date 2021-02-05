import { promisify } from 'util';

import xml2js from 'xml2js';

import get from '../get';
import marshall from '../marshall';
import expected from './catan.json';

const parseString = promisify(xml2js.parseString);
const baseURL = 'https://api.geekdo.com/xmlapi2/things';
test('Catan marshalled output matches', async () => {
  const stableFields = [
    'api-version',
    'id',
    'primary-name',
    'alternate-names',
    'description',
    'year',
    'minimum-players',
    'maximum-players',
    'minimum-playtime',
    'maximum-playtime',
    'minimum-age',
    'designers',
  ];

  const actual = await get(`${baseURL}?id=13&stats=1&type=boardgame,boardgameexpansion`)
    .then(parseString)
    .then((body) => marshall(body.items.item[0]));

  for (const field of stableFields) { // eslint-disable-line
    expect(actual[field]).toEqual(expected[field]);
  }
});

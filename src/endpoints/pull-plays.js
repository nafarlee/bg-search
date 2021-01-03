#!/usr/bin/env node
const { promisify } = require('util');

const parseString = promisify(require('xml2js').parseString);
const { Client } = require('pg');
const _ = require('lodash');

const get = require('../get');
const throttle = require('../throttle');
const { toSQL } = require('../db/insert');
const credentials = require('../../db-credentials');


const packPlay = (gameID) => (play) => [
  play.$.id,
  gameID,
  play.$.length,
  _.get(play, 'players[0].player.length', null),
];


async function getPlays(id, page) {
  const baseURL = 'https://www.boardgamegeek.com/xmlapi2/plays';
  const xml = await get(`${baseURL}?type=thing&subtype=boardgame&id=${id}&page=${page}`);
  const body = await parseString(xml);
  const plays = body.plays.play || [];
  return plays.map(packPlay(id));
}


async function getCheckpoint(client) {
  const {
    rows: [{ play_id: playID, play_page: playPage }],
  } = await client.query('SELECT play_id, play_page FROM globals');
  return [playID, playPage];
}


async function getLastGameID(client) {
  const {
    rows: [{ id }],
  } = await client.query('SELECT id FROM games ORDER BY id DESC LIMIT 1');
  return id;
}


async function saveCheckpoint({
  res,
  client,
  playID,
  playPage,
}) {
  await client.query('UPDATE globals SET play_id=$1, play_page=$2 WHERE id=1', [playID, playPage]);
  await client.end();
  return res.status(200).send();
}


async function savePage({
  res,
  client,
  playPage,
  nonZeroPlays,
  playID,
}) {
  try {
    await client.query('BEGIN');
    await client.query('UPDATE globals SET play_id=$1, play_page=$2 WHERE id=1', [playID, playPage + 1]);
    await client.query(...toSQL(
      'plays',
      ['id', 'game_id', 'length', 'players'],
      ['id'],
      nonZeroPlays,
    ));
    await client.query('COMMIT');
    return res.status(200).send();
  } catch (err) {
    console.error(err);
    console.error(`ERROR: id:${playID} page:${playPage}`);
    await client.query('ROLLBACK');
    return res.status(500).send();
  } finally {
    await client.end();
  }
}


module.exports = async function pullPlays(_req, res) {
  const start = Date.now();
  const timeout = 50 * 1000;

  const getPlaysSlowly = throttle(getPlays, 5 * 1000);

  const client = new Client(credentials);
  await client.connect();

  const lastGameID = await getLastGameID(client);
  let [playID, playPage] = await getCheckpoint(client);
  console.log(JSON.stringify({ type: 'start', game_id: playID, play_page_id: playPage }));

  while (start + timeout > Date.now()) {
    const plays = await getPlaysSlowly(playID, playPage); // eslint-disable-line no-await-in-loop
    const nonZeroPlays = plays.filter(([,, length]) => length > 0);

    if (_.isEmpty(plays) && playID === lastGameID) {
      playID = 1;
      playPage = 1;
    } else if (_.isEmpty(plays) && playID !== lastGameID) {
      playID += 1;
      playPage = 1;
    } else if (_.isEmpty(nonZeroPlays)) {
      playPage += 1;
    } else {
      console.log(JSON.stringify({ type: 'save_plays', game_id: playID, play_page_id: playPage }));
      return savePage({
        res,
        client,
        playPage,
        playID,
        nonZeroPlays,
      });
    }
  }

  console.log(JSON.stringify({ type: 'pause', game_id: playID, play_page_id: playPage }));
  return saveCheckpoint({
    res,
    client,
    playPage,
    playID,
  });
};

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


const log = (type, gameID, playPageID) => {
  console.log(JSON.stringify({ type, 'game-id': gameID, 'play-page-id': playPageID }));
};


const getPlays = async (id, page) => {
  const baseURL = 'https://www.boardgamegeek.com/xmlapi2/plays';
  const xml = await get(`${baseURL}?type=thing&subtype=boardgame&id=${id}&page=${page}`);
  const body = await parseString(xml);
  const plays = body.plays.play || [];
  return plays.map(packPlay(id));
};


const getCheckpoint = async (client) => {
  const {
    rows: [{ play_id: playID, play_page: playPage }],
  } = await client.query('SELECT play_id, play_page FROM globals');
  return [playID, playPage];
};


const getLastGameID = async (client) => {
  const {
    rows: [{ id }],
  } = await client.query('SELECT id FROM games ORDER BY id DESC LIMIT 1');
  return id;
};


const saveCheckpoint = async ({
  res,
  client,
  playID,
  playPage,
}) => {
  await client.query('UPDATE globals SET play_id=$1, play_page=$2 WHERE id=1', [playID, playPage]);
  await client.end();
  return res.status(200).send();
};


const savePage = async ({
  client,
  playPage,
  nonZeroPlays,
  playID,
}) => {
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
  } catch (err) {
    await client.query('ROLLBACK');
    throw err;
  }
};


const isExistingGame = async (client, gameID) => (
  (await client.query('SELECT 1 FROM games WHERE id=$1 LIMIT 1', [gameID])).rowCount === 1
);


module.exports = async (_req, res) => {
  const start = Date.now();
  const timeout = 9 * 60 * 1000;

  const getPlaysSlowly = throttle(getPlays, 5 * 1000);

  const client = new Client(credentials);
  await client.connect();

  const lastGameID = await getLastGameID(client);
  let [playID, playPage] = await getCheckpoint(client);

  while (start + timeout > Date.now()) {
    if (!await isExistingGame(client, playID)) { // eslint-disable-line no-await-in-loop
      log('ignore-non-game', playID, playPage);
      playID += 1;
      playPage = 1;
      continue;
    }

    const plays = await getPlaysSlowly(playID, playPage); // eslint-disable-line no-await-in-loop
    const areNoPlays = _.isEmpty(plays);
    const isLastGame = playID === lastGameID;
    if (areNoPlays && isLastGame) {
      log('mobius', playID, playPage);
      playID = 1;
      playPage = 1;
      continue;
    }

    if (areNoPlays && !isLastGame) {
      log('next-game', playID, playPage);
      playID += 1;
      playPage = 1;
      continue;
    }

    const nonZeroPlays = plays.filter(([,, length]) => length > 0);
    if (_.isEmpty(nonZeroPlays)) {
      log('skip-page', playID, playPage);
      playPage += 1;
      continue;
    }

    try {
      await savePage({ // eslint-disable-line no-await-in-loop
        client,
        playPage,
        playID,
        nonZeroPlays,
      });
      log('save-plays', playID, playPage);
      playPage += 1;
    } catch (err) {
      console.error(err);
      log('save-plays-error', playID, playPage);
      await client.end(); // eslint-disable-line no-await-in-loop
      return res.status(500).send();
    }
  }

  log('pause', playID, playPage);
  return saveCheckpoint({
    res,
    client,
    playPage,
    playID,
  });
};

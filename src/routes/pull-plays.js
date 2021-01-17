const { promisify } = require('util');

const parseString = promisify(require('xml2js').parseString);
const { Client } = require('pg');
const _ = require('lodash');

const get = require('../get');
const throttle = require('../throttle');
const { toSQL } = require('../db/insert');
const credentials = require('../../db-credentials');
const T = require('../T');


const packPlay = (gameID) => (play) => [
  play.$.id,
  gameID,
  play.$.length,
  _.get(play, 'players[0].player.length', null),
];


const log = (type, gameID, playPageID) => {
  console.log(JSON.stringify({ type, 'game-id': gameID, 'play-page-id': playPageID }));
};


const getPlays = throttle(async (id, page) => {
  const baseURL = 'https://www.boardgamegeek.com/xmlapi2/plays';
  const xml = await get(`${baseURL}?type=thing&subtype=boardgame&id=${id}&page=${page}`);
  const body = await parseString(xml);
  const plays = body.plays.play || [];
  return plays.map(packPlay(id));
}, 5 * 1000);


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
  log('pause', playID, playPage);
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
    log('save-plays', playID, playPage);
    return [playID, playPage + 1];
  } catch (err) {
    await client.query('ROLLBACK');
    console.error(err);
    log('save-plays-error', playID, playPage);
    throw err;
  }
};


const isExistingGame = async (client, gameID) => (
  (await client.query('SELECT 1 FROM games WHERE id=$1 LIMIT 1', [gameID])).rowCount === 1
);


const ignoreNonGame = (ID, page) => {
  log('ignore-non-game', ID, page);
  return [ID + 1, 1];
};


const mobius = (ID, page) => {
  log('mobius', ID, page);
  return [1, 1];
};


const nextGame = (ID, page) => {
  log('next-game', ID, page);
  return [ID + 1, 1];
};


const skipPage = (ID, page) => {
  log('skip-page', ID, page);
  return [ID, page + 1];
};


module.exports = async (_req, res) => {
  const start = Date.now();
  const timeout = 9 * 60 * 1000;

  const client = new Client(credentials);
  await client.connect();

  const lastGameID = await getLastGameID(client);
  let [playID, playPage] = await getCheckpoint(client);

  while (start + timeout > Date.now()) {
    if (!await isExistingGame(client, playID)) { // eslint-disable-line no-await-in-loop
      [playID, playPage] = ignoreNonGame(playID, playPage);
      continue;
    }

    const plays = await getPlays(playID, playPage); // eslint-disable-line no-await-in-loop
    const areNoPlays = _.isEmpty(plays);
    const isLastGame = playID === lastGameID;
    if (areNoPlays && isLastGame) {
      [playID, playPage] = mobius(playID, playPage);
      continue;
    }

    if (areNoPlays && !isLastGame) {
      [playID, playPage] = nextGame(playID, playPage);
      continue;
    }

    const nonZeroPlays = plays.filter(([,, length]) => length > 0);
    if (_.isEmpty(nonZeroPlays)) {
      [playID, playPage] = skipPage(playID, playPage);
      continue;
    }

    const [saveError, saveResult] = await T(savePage({ // eslint-disable-line no-await-in-loop
      client,
      playPage,
      playID,
      nonZeroPlays,
    }));
    if (saveError) {
      await client.end(); // eslint-disable-line no-await-in-loop
      return res.status(500).send(saveError);
    }
    [playID, playPage] = saveResult;
  }

  return saveCheckpoint({
    res,
    client,
    playPage,
    playID,
  });
};

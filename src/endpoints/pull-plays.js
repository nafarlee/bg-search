#!/usr/bin/env node
const { promisify } = require('util');

const parseString = promisify(require('xml2js').parseString);
const { Client } = require('pg');
const _ = require('lodash');

const get = require('../get');
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

const success = {
  async reset({ res, client }) {
    await client.query('UPDATE globals SET play_id = 1, play_page = 1 WHERE id = 1');
    await client.end();
    return res.status(200).send();
  },
};

async function pullPlays(_req, res) {
  const client = new Client(credentials);
  await client.connect();

  const [playID, playPage] = await getCheckpoint(client);
  const plays = await getPlays(playID, playPage);
  const lastGameID = await getLastGameID(client);

  if (_.isEmpty(plays) && playID === lastGameID) return success.reset({ res, client });

  if (_.isEmpty(plays) && playID !== lastGameID) {
    await client.query('UPDATE globals SET play_id=$1, play_page=1 WHERE id=1', [playID + 1]);
    await client.end();
    return res.status(200).send();
  }

  const nonZeroPlays = plays.filter(([,, length]) => length > 0);
  if (nonZeroPlays.length === 0) {
    await client.query('UPDATE globals SET play_page = $1 WHERE id = 1', [playPage + 1]);
    await client.end();
    return res.status(200).send();
  }

  try {
    await client.query('BEGIN');
    await client.query('UPDATE globals SET play_page = $1 WHERE id = 1', [playPage + 1]);
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

module.exports = pullPlays;

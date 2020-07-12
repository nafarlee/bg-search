#!/usr/bin/env node
const { promisify } = require('util');

const parseString = promisify(require('xml2js').parseString);
const { Client } = require('pg');
const _ = require('lodash');

const get = require('../get');
const { toSQL } = require('../db/insert');
const credentials = require('../../db-credentials');

async function getPlay(id, page) {
  const baseURL = 'https://www.boardgamegeek.com/xmlapi2/plays';
  const xml = await get(`${baseURL}?type=thing&id=${id}&page=${page}`);
  const body = await parseString(xml);
  const plays = body.plays.play;

  if (!plays) return [];

  return plays
    .filter((p) => p.$.length !== '0')
    .map((p) => [
      p.$.id,
      id,
      p.$.length,
      _.get(p, 'players[0].player.length', null),
    ]);
}

async function pullPlays(_req, res) {
  const client = new Client(credentials);

  await client.connect();
  const {
    rows: [{ play_id: playID, play_page: playPage }],
  } = await client.query('SELECT play_id, play_page FROM globals');

  const plays = await getPlay(playID, playPage);
  if (!plays) {
    const {
      rows: [{ id: lastGame }],
    } = await client.query('SELECT id FROM games ORDER BY id DESC LIMIT 10');
    if (playID === lastGame) {
      await client.query('UPDATE globals SET play_id = 1, play_page = 1 WHERE id = 1');
      return res.status(200).send();
    }
    await client.query(
      'UPDATE globals SET play_id = $1, play_page = 1 WHERE id = 1',
      [playID + 1],
    );
    return res.status(200).send();
  }

  await client.query('BEGIN');
  await client.query('UPDATE globals SET play_page = $1 WHERE id = 1', [playPage + 1]);
  await client.query(...toSQL('plays', ['id', 'game_id', 'length', 'players'], ['id'], plays));
  try {
    await client.query('COMMIT');
    return res.status(200).send();
  } catch (err) {
    await client.query('ROLLBACK');
    return res.status(500).send();
  } finally {
    await client.end();
  }
}

module.exports = pullPlays;

#!/usr/bin/env node
const { promisify } = require('util');

const parseString = promisify(require('xml2js').parseString);
const _ = require('lodash');

const get = require('../get');

async function getPlay(id, page) {
  const baseURL = 'https://www.boardgamegeek.com/xmlapi2/plays';
  const xml = await get(`${baseURL}?type=thing&id=${id}&page=${page}`);
  const body = await parseString(xml);
  const plays = body.plays.play;

  if (!plays) return [];

  return plays
    .filter((p) => p.$.length !== '0')
    .map((p) => ({
      gameID: id,
      playID: p.$.id,
      length: p.$.length,
      players: _.get(p, 'players[0].player.length', null),
    }));
}

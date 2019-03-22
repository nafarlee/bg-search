const { promisify } = require('util');

const parseString = promisify(require('xml2js').parseString);

const get = require('./src/get');
const marshall = require('./src/marshall');

function randomInt(max) {
  return Math.floor(Math.random() * max);
}

const baseURL = 'https://api.geekdo.com/xmlapi2/things';
const id = [...Array(500).keys()].map(() => randomInt(270000));
const start = process.hrtime();
get(`${baseURL}?id=${id}&stats=1&type=boardgame,boardgameexpansion`)
  .then(parseString)
  .then((body) => {
    if (!body.items.item) {
      return;
    }
    console.log(body.items.item.map(marshall));
  });

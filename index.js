const { promisify } = require('util');

const parseString = promisify(require('xml2js').parseString);

const get = require('./src/get');
const marshall = require('./src/marshall');

const baseURL = 'https://api.geekdo.com/xmlapi2/things';
const start = process.hrtime();
get(`${baseURL}?id=13&type=boardgame`)
  .then(parseString)
  .then((body) => {
    if (!body.items.item) {
      return;
    }
    const native = body.items.item.map(marshall);
    console.log(JSON.stringify(native, undefined, 2));
    console.error(process.hrtime(start));
  });

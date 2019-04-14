const { promisify } = require('util');

const parseString = promisify(require('xml2js').parseString);
const _ = require('lodash');
const functions = require('firebase-functions');
const admin = require('firebase-admin');

const get = require('./src/get');
const marshall = require('./src/marshall');
const language = require('./src/language');
const verify = require('./src/query');
const views = require('./src/views');

const baseURL = 'https://api.geekdo.com/xmlapi2/things';

admin.initializeApp();

exports.pull = functions
  .pubsub
  .topic('pull')
  .onPublish(async () => {
    const db = admin.firestore();
    const batch = db.batch();
    const doc = await db.collection('_').doc('_').get();

    if (!doc.exists) throw new Error('Metadata document not found!');
    const index = doc.get('index');
    const newIndex = index + 500;
    const IDs = _.range(index, newIndex).join(',');
    batch.update(db.collection('_').doc('_'), { index: newIndex });
    const xml = await get(`${baseURL}?stats=1&type=boardgame,boardgameexpansion&id=${IDs}`);

    const body = await parseString(xml);

    if (!body.items.item) {
      return db.collection('_').doc('_').update({ index: 1 });
    }

    body.items.item.forEach((item) => {
      const native = marshall(item);
      batch.set(db.collection('games').doc(`${native.id}`), native);
    });
    return batch.commit();
  });

exports.search = functions
  .runWith({ timeoutSeconds: 540 })
  .https
  .onRequest(async (req, res) => {
    res.set('Cache-Control', `public, max-age=${60 * 60 * 24 * 7}`);
    const query = req.query.query || '';
    const order = req.query.order || 'bayes-rating';
    const direction = req.query.direction || 'desc';
    console.log({ query, order, direction });
    const predicates = language.tryParse(query);

    const db = admin.firestore();
    const results = [];
    let col = db.collection('games').orderBy(order, direction);
    if (req.query.checkpoint) {
      const snapshot = await db
        .collection('games')
        .doc(req.query.checkpoint)
        .get();
      col = col.startAfter(snapshot);
    }
    return col
      .stream()
      .on('data', (doc) => {
        const data = doc.data();
        try {
          if (verify(predicates, data)) results.push(data);
        } catch (e) {
          console.error({ query, game: data });
          throw e;
        }
        if (results.length >= 10) {
          res.status(200).send(views.search({ req, fnName: 'search', games: results }));
        }
      })
      .on('end', () => res.status(200).send(views.search({ req, games: results })));
  });


const functions = require('firebase-functions');
const admin = require('firebase-admin');

const language = require('./src/language');
const verify = require('./src/query');
const views = require('./src/views');
const pull = require('./src/endpoints/pull');

admin.initializeApp();

exports.pull = functions
  .pubsub
  .topic('pull')
  .onPublish(pull);

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

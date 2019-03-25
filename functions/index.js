const { promisify } = require('util');

const parseString = promisify(require('xml2js').parseString);
const _ = require('lodash');
const functions = require('firebase-functions');
const admin = require('firebase-admin');

const get = require('./src/get');
const marshall = require('./src/marshall');

const baseURL = 'https://api.geekdo.com/xmlapi2/things';

admin.initializeApp();

exports.pull = functions
  .pubsub
  .topic('pull')
  .onPublish(() => {
    const db = admin.firestore();
    const batch = db.batch();
    return db
      .collection('_')
      .doc('_')
      .get()
      .then((doc) => {
        if (!doc.exists) throw new Error('Metadata document not found!');
        const index = doc.get('index');
        const newIndex = index + 499;
        const IDs = _.range(index, newIndex).join(',');
        batch.update(db.collection('_').doc('_'), { index: newIndex });
        return get(
          `${baseURL}?stats=1&type=boardgame,boardgameexpansion&id=${IDs}`,
        );
      })
      .then(parseString)
      .then((body) => {
        if (!body.items.item) throw new Error('No BGG documents found!');
        body.items.item.forEach((item) => {
          const native = marshall(item);
          batch.set(db.doc(`games/${native.id}`), native);
        });
        return batch.commit();
      });
  });

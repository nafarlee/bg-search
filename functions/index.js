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
      return batch.update(db.collection('_').doc('_'), { index: 1 });
    }

    body.items.item.forEach((item) => {
      const native = marshall(item);
      batch.set(db.collection('games').doc(`${native.id}`), native);
    });
    return batch.commit();
  });

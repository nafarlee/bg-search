const functions = require('firebase-functions');
const admin = require('firebase-admin');

const pull = require('./src/endpoints/pull');
const search = require('./src/endpoints/search');

admin.initializeApp();

exports.pull = functions
  .pubsub
  .topic('pull')
  .onPublish(pull);

exports.search = functions
  .runWith({ timeoutSeconds: 540 })
  .https
  .onRequest(search);

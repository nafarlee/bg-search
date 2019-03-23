const functions = require('firebase-functions');

exports.pull = functions
  .pubsub
  .topic('pull')
  .onPublish((message) => {
    console.log(`Firebase Functions just got a message: ${message}`);
    return message;
  });

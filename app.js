const express = require('express');

const pull = require('./src/endpoints/pull');
const search = require('./src/endpoints/search');

const app = express();

app.get('/search', search);
app.post('/pubsub/push/pull', pull);

app.listen(process.env.PORT, () => {});

module.exports = app;

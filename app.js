const express = require('express');

const pull = require('./src/endpoints/pull');
const search = require('./src/endpoints/search');
const pullPlays = require('./src/endpoints/pull-plays');

const app = express();
app.set('view engine', 'pug');

app.get('/search', search);
app.post('/pubsub/pull', pull);
app.post('/pubsub/pull-plays', pullPlays);

const PORT = process.env.PORT || 8080;
app.listen(PORT, () => {});

module.exports = app;

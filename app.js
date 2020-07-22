const express = require('express');

const pull = require('./src/endpoints/pull');
const search = require('./src/endpoints/search');
const pullPlays = require('./src/endpoints/pull-plays');
const games = require('./src/endpoints/games');
const locals = require('./views/locals');

const app = express();
app.set('view engine', 'pug');
app.locals = { ...app.locals, ...locals };

app.get('/search', search);
app.post('/pubsub/pull', pull);
app.post('/pubsub/pull-plays', pullPlays);
app.get('/games/:id', games);

const PORT = process.env.PORT || 8080;
app.listen(PORT, () => {});

module.exports = app;

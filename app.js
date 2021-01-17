const express = require('express');

const pull = require('./src/routes/pull');
const search = require('./src/routes/search');
const pullPlays = require('./src/routes/pull-plays');
const games = require('./src/routes/games');
const locals = require('./views/locals');

const app = express();
app.set('view engine', 'pug');
app.locals = { ...app.locals, ...locals };

app.use(express.static('public'));
app.get('/search', search);
app.post('/pubsub/pull', pull);
app.post('/pubsub/pull-plays', pullPlays);
app.get('/games/:id', games);

const PORT = process.env.PORT || 8080;
app.listen(PORT, () => {});

module.exports = app;

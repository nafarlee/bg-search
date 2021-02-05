import _ from 'lodash';

import {
  age,
  description,
  id,
  links,
  name,
  players,
  playtime,
  ratings,
  weight,
  year,
} from './lib';

export default function marshall(game) {
  return {
    'api-version': 3,
    id: id(game),
    image: _.get(game, ['image', 0], null),
    thumbnail: _.get(game, ['thumbnail', 0], null),
    ...name(game),
    description: description(game),
    year: year(game),
    ...players(game),
    ...playtime(game),
    ...age(game),
    ...ratings(game),
    ...weight(game),
    ...links(game),
    'last-updated': (new Date()).toString(),
  };
}

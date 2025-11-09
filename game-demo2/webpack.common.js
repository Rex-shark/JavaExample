const path = require('path');

module.exports = {
  entry: {
    app: './js/app.js',
    devour_game: './js/devour_game.js',
    draw_game: './js/draw_game.js',
  },
  output: {
    path: path.resolve(__dirname, 'dist'),
    clean: true,
    filename: 'js/[name].js',
  },
};

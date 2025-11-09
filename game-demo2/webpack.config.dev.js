const { merge } = require('webpack-merge');
const common = require('./webpack.common.js');

module.exports = merge(common, {
  mode: 'development',
  devtool: 'inline-source-map',
  devServer: {
    port: 3000, // ★ 指定開發伺服器的啟動埠號
    liveReload: true,
    hot: true,
    open: true,
    static: ['./'],
    proxy: [
      {
        context: ['/move'],
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
      {
        // Proxy backend REST APIs for draw game
        context: ['/game'],
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
      // {
      //   // Proxy websocket for convenience if needed
      //   context: ['/ws', '/ws/game'],
      //   target: 'http://localhost:8080',
      //   changeOrigin: true,
      //   ws: true,
      // },
    ],
  },
});

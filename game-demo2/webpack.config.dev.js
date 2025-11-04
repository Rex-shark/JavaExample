const { merge } = require('webpack-merge');
const common = require('./webpack.common.js');

module.exports = merge(common, {
  mode: 'development',
  devtool: 'inline-source-map',
  devServer: {
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
      // {
      //   context: ['/ws','/wss', '/ws/game'],    // 新增：把 websocket 路徑代理到本機 8080
      //   target: 'https://k4sq98r8-8080.asse.devtunnels.ms',
      //   changeOrigin: true,
      //   ws: true,                         // 啟用 websocket 升級轉發
      // },
    ],
  },
});

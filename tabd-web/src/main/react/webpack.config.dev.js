var path = require('path')
var webpack = require('webpack')
var HtmlWebpackPlugin = require('html-webpack-plugin')

module.exports = {
  devtool: 'cheap-eval-source-map',
  entry: [
    'webpack-dev-server/client?http://localhost:9902',
    'webpack/hot/dev-server',
    './src/index.jsx'
  ],
  resolve: {
    extensions: ['', '.js', '.jsx']
  },
  output: {
    path: path.join(__dirname, 'build'),
    filename: 'bundle.js',
    publicPath: '/'
  },
  plugins: [
    new webpack.HotModuleReplacementPlugin(),
    new HtmlWebpackPlugin({
      template: './src/index.html'
    })
  ],
  module: {
    loaders: [{
      test: /\.(js|jsx)$/,
      loaders: ['babel'],
      include: path.join(__dirname,'src')
    },{
      test: /\.scss$/,
      loaders: ['style', 'css', 'resolve-url', 'sass?sourceMap']
    },{
      test: /\.(png|jpg)$/,
      loader: 'url-loader?limit=8192'
    }]
  },
  devServer: {
    contentBase: './build',
    proxy: {
      "/api/**": {
        target: "http://localhost:9900/"
      }
    },
    historyApiFallback: true,
    hot: true
  }
}

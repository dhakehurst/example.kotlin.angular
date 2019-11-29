var UglifyJsPlugin = require('terser-webpack-plugin');

module.exports = {
  optimization: {
    minimize: false,
    //minimizer: [new TerserPlugin()],
  },
};

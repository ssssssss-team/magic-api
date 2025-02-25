const MonacoLocalesPlugin = require('./plugins/MonacoEditorLocalesPlugin.js')
const path = require('path')
const webpack = require('webpack')
const resolve = dir => {
  return path.join(__dirname, dir)
}
// 设置环境变量，可以在全局使用
process.env.VUE_APP_MA_VERSION = require('./package.json').version

module.exports = {
  publicPath: './',
  productionSourceMap: false,
  configureWebpack: {
    output: {
      libraryExport: 'default'
    },
    module: {
      rules:[{
        test: /\.worker.js$/,
        exclude: /node_modules/,
        use: [{
          loader: 'worker-loader',
          options: {
            inline: 'fallback'
          }
        }]
      }]
    },
    plugins: [
      new MonacoLocalesPlugin({
        //设置支持的语言
        languages: ['zh-cn'],
        //默认语言
        defaultLanguage: 'zh-cn',
        //打印不匹配的文本
        logUnmatched: false,
        //自定义文本翻译
        // mapLanguages: { 'zh-cn': { 'Peek References': '查找引用', 'Go to Symbol...': '跳到变量位置', 'Command Palette': '命令面板' } }
      })
    ]
  },
  parallel: false,
  chainWebpack: config => {
    config.resolve.alias
      .set('@', resolve('src')) // key,value自行定义，比如.set('@@', resolve('src/components'))
      .set('public', resolve('public'))
    // 移除 prefetch 插件
    config.plugins.delete('prefetch')
    // 移除 preload 插件
    config.plugins.delete('preload')
    config.output.globalObject('this')
    config.output.filename('js/[name].[hash].js').end()
  }
}

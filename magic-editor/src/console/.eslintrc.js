module.exports = {
  root: true,
  env: {
    node: true
  },
  'extends': [
    'plugin:vue/essential'
    // '@vue/standard'
  ],
  rules: {
    'no-console': 'off',
    'no-debugger': process.env.NODE_ENV === 'production' ? 'error' : 'off',
    // allow async-await
    'generator-star-spacing': 'off',
    'space-before-function-paren': 0,
    "vue/no-parsing-error": [2, {
      "x-invalid-end-tag": false
    }],
    'space-in-parens': [0, 'never'] //小括号里面要不要有空格
  },
  parserOptions: {
    parser: 'babel-eslint'
  }
}

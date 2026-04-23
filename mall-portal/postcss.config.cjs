module.exports = {
  plugins: {
    'postcss-px-to-viewport': {
      viewportWidth: 750,
      unitPrecision: 5,
      viewportUnit: 'vw',
      selectorBlackList: ['.ignore-vw', '.hairlines'],
      minPixelValue: 1,
    },
    autoprefixer: {},
  },
}

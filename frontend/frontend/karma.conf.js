module.exports = function (config) {
  config.set({
    basePath: '',
    frameworks: ['jasmine', '@angular-devkit/build-angular'],
    plugins: [
      require('karma-jasmine'),
      require('karma-chrome-launcher'),
      require('karma-jasmine-html-reporter'),
      require('karma-coverage'),
      require('@angular-devkit/build-angular/plugins/karma')
    ],
    client: {
      clearContext: false // deja visible el resultado en el navegador
    },
    coverageReporter: {
      dir: require('path').join(__dirname, './coverage'),
      subdir: '.',
      reporters: [
        { type: 'html' },         // genera reporte visual navegable
        { type: 'text-summary' }  // muestra resumen en consola
      ],
      includeAllSources: true,     // ✅ INCLUYE TODOS LOS .ts aunque solo estén en .spec.ts
      instrumenterOptions: {
        istanbul: { noCompact: true } // ✅ importante para líneas dentro de funciones flecha
      },
      check: {
        global: {
          statements: 10,
          branches: 5,
          functions: 5,
          lines: 10
        }
      }
    },
    reporters: ['progress', 'kjhtml', 'coverage'],
    port: 9876,
    colors: true,
    logLevel: config.LOG_INFO,
    autoWatch: true,
    browsers: ['ChromeHeadless'],
    singleRun: true,
    restartOnFileChange: false
  });
};

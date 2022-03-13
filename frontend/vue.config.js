const { defineConfig } = require("@vue/cli-service");
module.exports = defineConfig({
  transpileDependencies: true,
  chainWebpack: (config) => {
    // config.optimization.minimize(false);
    config.plugin("html").tap((args) => {
      args[0].title = "More Fair Game";
      return args;
    });
  },
});

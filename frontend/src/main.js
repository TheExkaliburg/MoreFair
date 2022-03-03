import { createApp } from "vue";
import App from "./App.vue";
import router from "./router";
import store from "./store";

let test = store;
createApp(App).use(test).use(router).mount("#app");

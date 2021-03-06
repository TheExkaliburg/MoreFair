import "bootstrap/dist/css/bootstrap.min.css";
import "bootstrap/dist/js/bootstrap.bundle.js";
import "bootstrap-icons/font/bootstrap-icons.css";
import "./styles/styles.scss";
import { createApp } from "vue";
import App from "./App.vue";
import router from "./router";
import store from "./store";
import vfmPlugin from "vue-final-modal";
import Fair from "@/scripting/scriptingAPI";

const app = createApp(App).use(store).use(router).use(vfmPlugin);

app.config.performance = true;

app.mount("#app");
window.Fair = Fair;

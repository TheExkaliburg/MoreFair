import { createRouter, createWebHistory } from "vue-router";
import GameView from "@/views/GameView.vue";
import OptionsView from "@/views/OptionsView.vue";
import HelpView from "@/views/HelpView.vue";
import ModerationView from "@/views/ModerationView";

const routes = [
  {
    path: "/",
    name: "home",
    component: GameView,
  },
  {
    path: "/help",
    name: "help",
    component: HelpView,
  },
  {
    path: "/options",
    name: "options",
    component: OptionsView,
  },
  {
    path: "/mod",
    name: "mod",
    component: ModerationView,
  },
];

const router = createRouter({
  history: createWebHistory(process.env.BASE_URL),
  routes,
});

export default router;

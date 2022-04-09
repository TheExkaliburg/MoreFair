import { createRouter, createWebHistory } from "vue-router";
import GameView from "@/views/GameView.vue";
import OptionsView from "@/views/OptionsView.vue";

const routes = [
  {
    path: "/",
    name: "home",
    component: GameView,
  },
  {
    path: "/help",
    name: "help",
    // route level code-splitting
    // this generates a separate chunk (about.[hash].js) for this route
    // which is lazy-loaded when the route is visited.
    component: () =>
      import(/* webpackChunkName: "about" */ "@/views/HelpView.vue"),
  },
  {
    path: "/options",
    name: "options",
    component: OptionsView,
  },
];

const router = createRouter({
  history: createWebHistory(process.env.BASE_URL),
  routes,
});

export default router;

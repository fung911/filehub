import { createRouter, createWebHashHistory as createWebHistory } from "vue-router";
import FilesView from "./views/FilesView.vue";
import LoginView from "./views/LoginView.vue";
import { authState } from "./stores/auth";

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: "/", redirect: "/login" },
    { path: "/login", component: LoginView },
    { path: "/files", component: FilesView, meta: { requiresAuth: true } }
  ]
});

router.beforeEach((to) => {
  if (to.meta.requiresAuth && !authState.token) {
    return "/login";
  }
  if (to.path === "/login" && authState.token) {
    return "/files";
  }
});

export default router;

import Layout from "@/layout/layout.vue";
import Error from "@/views/error.vue";
import { RouteRecordRaw } from "vue-router";
import Login from "@/views/login.vue";
import Iframe from "@/views/iframe.vue";

/**
 * 框架基础路由
 */
const routes: Array<RouteRecordRaw> = [
  {
    path: "/",
    component: Layout,
    redirect: "/home",
    meta: { title: "工作台", icon: "icon-desktop" },
    children: [
      {
        path: "/home",
        component: () => import("@/views/home.vue"),
        meta: { title: "主页", icon: "icon-home" }
      }
    ]
  },

  /**
   * ✅ 定位签到模块
   * 包含两个子页面：
   *  - /attendance/geo-sign   普通用户签到
   *  - /attendance/geo-admin  管理员查看记录
   */
  {
    path: "/attendance",
    component: Layout,
    redirect: "/attendance/geo-admin",
    meta: { title: "定位签到", icon: "icon-location", requiresAuth: true },
    children: [
      {
        path: "/attendance/geo-sign",
        component: () => import("@/views/attendance/geo-sign.vue"),
        meta: { title: "定位签到", icon: "icon-location", requiresAuth: true }
      },
      {
        path: "/attendance/geo-admin",
        component: () => import("@/views/attendance/geo-admin.vue"),
        meta: { title: "签到记录（管理员）", icon: "icon-list", requiresAuth: true }
      }
    ]
  },

  /**
   * ✅ 收件箱模块（原有）
   */
  {
    path: "/inbox",
    component: Layout,
    redirect: "/inbox/index",
    meta: { title: "收件箱", icon: "icon-message", requiresAuth: true },
    children: [
      {
        path: "/inbox/index",
        component: () => import("@/views/sys/inbox.vue"),
        meta: { title: "收件箱", icon: "icon-inbox" }
      }
    ]
  },

  /**
   * ✅ 登录 / 密码修改 / iframe / 错误页（保持原逻辑）
   */
  {
    path: "/login",
    component: Login,
    meta: { title: "登录", isNavigationMenu: false }
  },
  {
    path: "/user/password",
    component: () => import("@/views/sys/user-update-password.vue"),
    meta: { title: "修改密码", requiresAuth: true, isNavigationMenu: false }
  },
  {
    path: "/iframe/:id?",
    component: Iframe,
    meta: { title: "iframe", isNavigationMenu: false }
  },
  {
    path: "/error",
    name: "error",
    component: Error,
    meta: { title: "错误页面", isNavigationMenu: false }
  },

  /**
   * ✅ 404 兜底
   */
  {
    path: "/:path(.*)*",
    redirect: { path: "/error", query: { to: 404 }, replace: true },
    meta: { isNavigationMenu: false }
  }
];

export default routes;

// src/utils/http.ts
import app from "@/constants/app";
import router from "@/router";
import axios, { AxiosRequestConfig } from "axios";
import qs from "qs";
import { ElMessage } from "element-plus";
import { getToken } from "./cache";
import { getValueByKeys } from "./utils";

// 优先读取环境变量，兼容旧写法
const http = axios.create({
  baseURL: (import.meta as any).env?.VITE_APP_API || (app as any).api || "/renren-admin",
  timeout: (app as any).requestTimeout ?? 10000,
});

http.interceptors.request.use(
  function (config: any) {
    config.headers["X-Requested-With"] = "XMLHttpRequest";
    config.headers["Request-Start"] = Date.now();

    const token = getToken();
    if (token) {
      config.headers["token"] = token;
    }

    // GET 防缓存
    if (config.method?.toUpperCase() === "GET") {
      config.params = { ...(config.params || {}), _t: Date.now() };
    }

    // 表单提交
    if (Object.values(config.headers || {}).includes("application/x-www-form-urlencoded")) {
      config.data = qs.stringify(config.data);
    }

    return config;
  },
  function (error) {
    return Promise.reject(error);
  }
);

http.interceptors.response.use(
  (response) => {
    // 下载场景（blob/arraybuffer）直接放行
    const respType = response?.config?.responseType;
    if (respType === "blob" || respType === "arraybuffer") {
      return response;
    }

    // 如果后端明确返回未授权，跳登录（其余业务码不拦截）
    const code = response?.data?.code ?? response?.data?.status;
    if (code === 401 || code === "401") {
      router.replace("/login");
      return Promise.reject(new Error("未授权，请登录"));
    }

    // ✅ 其余一律放行，让页面自己用 isOk 判定
    return response;
  },
  (error) => {
    const status = getValueByKeys(error, "response.status", 500);
    const url = getValueByKeys(error, "response.config.url", "");
    const httpCodeLabel: Record<number, string> = {
      400: "请求参数错误",
      401: "未授权，请登录",
      403: "拒绝访问",
      404: `请求地址出错: ${url}`,
      408: "请求超时",
      500: "API接口报500错误",
      501: "服务未实现",
      502: "网关错误",
      503: "服务不可用",
      504: "网关超时",
      505: "HTTP版本不受支持",
    };

    if (error?.response) {
      console.error("请求错误", error.response.data);
    }

    if (status === 401) {
      router.replace("/login");
    }

    const msg = httpCodeLabel[status] || "接口错误";
    ElMessage.error(msg);
    return Promise.reject(new Error(msg));
  }
);

// 统一导出：普通请求返回 data；下载（blob/arraybuffer）返回整个 response
export default (o: AxiosRequestConfig) =>
  new Promise((resolve, reject) => {
    http(o)
      .then((res) => {
        const respType = (o as any)?.responseType;
        if (respType === "blob" || respType === "arraybuffer") {
          resolve(res as any);
        } else {
          resolve((res as any).data);
        }
      })
      .catch(reject);
  });

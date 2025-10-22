import { CacheToken } from "@/constants/cacheKey";
import { ICacheOptions } from "@/types/interface";
import { isNullOrUndefined } from "./utils";

const fix = "v1@";

/**
 * 存储介质适配器
 * @param isSessionStorage
 * @returns
 */
const cacheAdapter = (isSessionStorage?: boolean) => {
  return isSessionStorage ? sessionStorage : localStorage;
};

/**
 * 取缓存值
 * @param {*} key
 * @param {*} options
 */
export const getCache = (key: string, options?: ICacheOptions, defaultValue?: unknown): any => {
  key = fix + key;
  options = { isParse: true, isDelete: false, ...options };
  try {
    const value = cacheAdapter(options.isSessionStorage).getItem(key);
    if (options.isDelete) {
      cacheAdapter(options.isSessionStorage).removeItem(key);
    }
    return isNullOrUndefined(value)
      ? defaultValue
      : options.isParse
      ? value
        ? JSON.parse(value)
        : defaultValue
      : value;
  } catch (error) {
    console.error("getCache", error);
    return defaultValue;
  }
};

/**
 * 设置缓存值
 * @param {*} key
 * @param {*} value
 */
export const setCache = (
  key: string,
  value: string | Record<string, unknown> | Array<any>[],
  isSessionStorage?: boolean
): void => {
  key = fix + key;
  cacheAdapter(isSessionStorage).setItem(
    key,
    typeof value === "object" ? JSON.stringify(value) : value
  );
};

/**
 * 清除缓存
 * @param key
 * @param isSessionStorage
 */
export const removeCache = (key: string, isSessionStorage?: boolean): void => {
  key = fix + key;
  cacheAdapter(isSessionStorage).removeItem(key);
};

export const getToken = (): string => {
  const cache = getCache(CacheToken, { isSessionStorage: true });
  if (!cache) return "";

  // 如果缓存值是字符串类型，直接返回该字符串（即 token）
  if (typeof cache === "string") {
    return cache;
  }

  // 如果缓存值是对象并且包含 token 字段，则返回 token
  if (typeof cache === "object" && cache.token) {
    return cache.token;
  }

  return "";  // 如果没有 token，则返回空字符串
};


import { IHttpResponse, IObject } from "@/types/interface";
import http from "@/utils/http";
import { getToken } from "@/utils/cache";

/**
 * 常用CRUD + download
 */
export default {
  // 兼容 REST 风格：/path/:id 无请求体
  delete(path: string, params?: IObject): Promise<IHttpResponse> {
    return http({
      url: path,
      data: params,
      method: "DELETE"
    });
  },

  get(path: string, params?: IObject, headers?: IObject): Promise<IHttpResponse> {
    return new Promise((resolve, reject) => {
      http({
        url: path,
        params,
        headers,
        method: "GET"
      })
        .then(resolve)
        .catch((error) => {
          if (error !== "-999") reject(error);
        });
    });
  },

  put(path: string, params?: IObject, headers?: IObject): Promise<IHttpResponse> {
    return http({
      url: path,
      data: params,
      headers: { "Content-Type": "application/json;charset=UTF-8", ...headers },
      method: "PUT"
    });
  },

  post(path: string, body?: IObject, headers?: IObject): Promise<IHttpResponse> {
    return http({
      url: path,
      data: body,
      headers: { "Content-Type": "application/json;charset=UTF-8", ...headers },
      method: "POST"
    });
  },

  download(url: string, params: IObject = {}, filename: string): Promise<IHttpResponse> {
    return new Promise((resolve, reject) => {
      const requestParams = Object.keys(params).length > 0 ? params : {};
      const token = getToken();

      http({
        url,
        method: "GET",
        params: requestParams,
        responseType: "blob",
        headers: token ? { token } : undefined
      })
        .then((response: any) => {
          const contentType = response.data?.type || "";
          if (contentType.includes("application/json")) {
            const reader = new FileReader();
            reader.onload = () => {
              try {
                const errData = JSON.parse(reader.result as string);
                reject(new Error(errData.msg || "导出失败"));
              } catch (e) {
                reject(new Error("服务器返回异常格式"));
              }
            };
            reader.readAsText(response.data);
            return;
          }

          const blob = new Blob([response.data]);
          const downloadUrl = window.URL.createObjectURL(blob);
          const link = document.createElement("a");
          link.href = downloadUrl;
          link.download = filename || "download.xlsx";
          document.body.appendChild(link);
          link.click();
          document.body.removeChild(link);
          window.URL.revokeObjectURL(downloadUrl);

          resolve(response);
        })
        .catch((err) => {
          if (err !== "-999") reject(err);
        });
    });
  }
};

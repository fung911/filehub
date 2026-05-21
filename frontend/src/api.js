import { authState, clearSession } from "./stores/auth";

export async function api(path, options = {}) {
  const headers = new Headers(options.headers || {});
  if (authState.token) {
    headers.set("Authorization", `Bearer ${authState.token}`);
  }

  const response = await fetch(path, { ...options, headers });
  const contentType = response.headers.get("content-type") || "";
  const body = contentType.includes("application/json") ? await response.json() : await response.text();

  if (!response.ok) {
    if (response.status === 401 || response.status === 403) {
      clearSession();
    }
    const message = typeof body === "object" && body.message ? body.message : `请求失败：${response.status}`;
    throw new Error(message);
  }

  return body;
}

export async function downloadFile(file) {
  const response = await fetch(`/api/files/${file.id}/download`, {
    headers: { Authorization: `Bearer ${authState.token}` }
  });

  if (!response.ok) {
    throw new Error(`下载失败：${response.status}`);
  }

  const blob = await response.blob();
  const href = URL.createObjectURL(blob);
  const anchor = document.createElement("a");
  anchor.href = href;
  anchor.download = file.originalFilename || "downloaded-file";
  document.body.appendChild(anchor);
  anchor.click();
  anchor.remove();
  URL.revokeObjectURL(href);
}

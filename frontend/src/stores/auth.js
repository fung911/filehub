import { reactive } from "vue";

const tokenKey = "filehub_token";
const usernameKey = "filehub_username";

export const authState = reactive({
  token: localStorage.getItem(tokenKey) || "",
  username: localStorage.getItem(usernameKey) || ""
});

export function saveSession(token, username) {
  authState.token = token;
  authState.username = username;
  localStorage.setItem(tokenKey, token);
  localStorage.setItem(usernameKey, username);
}

export function clearSession() {
  authState.token = "";
  authState.username = "";
  localStorage.removeItem(tokenKey);
  localStorage.removeItem(usernameKey);
}

import axios from "axios";

const BASE = "http://localhost:8080/api";

function getToken() {
  return (
    localStorage.getItem("token") ||
    localStorage.getItem("jwt") ||
    localStorage.getItem("accessToken")
  );
}

function authHeader() {
  const t = getToken();
  return t ? { Authorization: "Bearer " + t } : {};
}

/**
 * Returns all users from /api/users (ADMIN only).
 */
export function getUsers() {
  return axios.get(`${BASE}/users`, {
    headers: authHeader(),
  });
}

/**
 * Returns all roles from /api/roles (ADMIN only).
 */
export function getRoles() {
  return axios.get(`${BASE}/roles`, {
    headers: authHeader(),
  });
}

/**
 * Creates a new user via /api/users (ADMIN action).
 * Uses the same fields as RegisterDto.
 */
export function createUser({ name, username, email, password }) {
  return axios.post(
    `${BASE}/users`,
    { name, username, email, password },
    { headers: authHeader() }
  );
}

/**
 * Updates an existing user via /api/users/{id}.
 */
export function updateUser(id, payload) {
  return axios.put(`${BASE}/users/${id}`, payload, {
    headers: authHeader(),
  });
}

/**
 * Deletes a user via /api/users/{id}.
 */
export function deleteUser(id) {
  return axios.delete(`${BASE}/users/${id}`, {
    headers: authHeader(),
  });
}

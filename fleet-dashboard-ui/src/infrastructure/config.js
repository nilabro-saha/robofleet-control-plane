/**
 * API base URL resolved from query param (apiBase) with localhost fallback.
 *
 * This module is test-safe and works when `window` is unavailable.
 *
 * @author Nilabro Saha
 */
const queryString = typeof window === "undefined"
  ? ""
  : window.location.search;
const params = new URLSearchParams(queryString);

export const apiBase = params.get("apiBase") ?? "http://localhost:8080";

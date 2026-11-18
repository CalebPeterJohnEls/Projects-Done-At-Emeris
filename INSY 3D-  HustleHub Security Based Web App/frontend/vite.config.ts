import { defineConfig } from "vitest/config";
import { loadEnv, type Plugin } from "vite";
import react from "@vitejs/plugin-react";
import fs from "fs";

// CSP for the page the browser loads (the API's helmet CSP doesn't cover it).
// Dev is looser because Vite hot reload needs an inline script + a websocket.
function cspPlugin(apiOrigin: string, isDev: boolean): Plugin {
  const dev = [
    "default-src 'self'",
    "script-src 'self' 'unsafe-inline'",
    "style-src 'self' 'unsafe-inline'",
    "img-src 'self' data:",
    `connect-src 'self' ${apiOrigin} wss://localhost:5173 ws://localhost:5173`,
    "object-src 'none'",
    "base-uri 'self'",
    "form-action 'self'",
  ];
  const prod = [
    "default-src 'self'",
    "script-src 'self'",
    "style-src 'self'",
    "img-src 'self' data:",
    `connect-src 'self' ${apiOrigin}`,
    "object-src 'none'",
    "base-uri 'none'",
    "form-action 'self'",
  ];
  return {
    name: "hustlehub-csp",
    transformIndexHtml(html) {
      const policy = (isDev ? dev : prod).join("; ");
      return html.replace(
        "<!--CSP-->",
        `<meta http-equiv="Content-Security-Policy" content="${policy}" />`,
      );
    },
  };
}

export default defineConfig(({ command, mode }) => {
  const env = loadEnv(mode, process.cwd(), "VITE_");
  const apiOrigin = env.VITE_API_ORIGIN || "https://localhost:3000";
  const isDev = command === "serve";

  // only read the certs when actually serving, so tests/builds work on a clean clone
  const keyPath = "../cert.key";
  const certPath = "../cert.crt";
  const https =
    isDev && mode !== "test" && fs.existsSync(keyPath) && fs.existsSync(certPath)
      ? { key: fs.readFileSync(keyPath), cert: fs.readFileSync(certPath) }
      : undefined;

  const headers = {
    "X-Content-Type-Options": "nosniff",
    "X-Frame-Options": "DENY",
    "Referrer-Policy": "no-referrer",
  };

  return {
    plugins: [react(), cspPlugin(apiOrigin, isDev)],
    server: { https, port: 5173, headers },
    preview: { https, port: 5173, headers },
    test: {
      environment: "jsdom",
      globals: true,
      setupFiles: "./src/tests/setup.ts", // <- your folder is "tests"
      css: false,
    },
  };
});

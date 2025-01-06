import react from "@vitejs/plugin-react-swc";
import * as path from "path";
import { defineConfig, loadEnv } from "vite";
import { checker } from "vite-plugin-checker";
import dts from "vite-plugin-dts";

// https://vitejs.dev/config/
// @ts-ignore
export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), "");
  const external = ["react", "react/jsx-runtime", "react-dom"];
  const plugins = [react(), checker({ typescript: true })];
  const input = env.LIB ? undefined : "src/main.tsx";
  if (env.LIB) {
    external.push("react-router-dom");
    external.push("react-i18next");
    plugins.push(dts({ insertTypesEntry: true }));
  }
  const lib = env.LIB
    ? {
        outDir: "lib",
        lib: {
          entry: path.resolve(__dirname, "src/index.ts"),
          formats: ["es"],
        },
      }
    : {
        outDir: "target/classes/theme/keycloak.v2/admin/resources",
        external: ["src/index.ts"],
      };
  return {
    base: "",
    server: {
      hmr: {
        protocol: "ws",
        host: "localhost",
        port: 5173,
        clientPort: 5173,
      },
      origin: "http://localhost:5173",
      port: 5173,
      watch: {
        usePolling: true,
        include: ["**/*.tsx", "**/*.ts", "**/*.css"],
      },
      proxy: {
        "/admin": {
          target: "http://localhost:9080",
          changeOrigin: true,
          secure: false,
          configure: (proxy) => {
            proxy.on("proxyRes", (proxyRes) => {
              proxyRes.headers["content-security-policy"] = `
                default-src 'self';
                script-src 'self' 'unsafe-inline' 'unsafe-eval' http://localhost:5173 http://localhost:9080 http://localhost:9000 http://localhost:4040;
                style-src 'self' 'unsafe-inline' 'unsafe-eval';
                frame-src 'self' http://localhost:9080 http://localhost:5173 http://localhost:9000 http://localhost:4040;
                frame-ancestors 'self' http://localhost:9080 http://localhost:5173 http://localhost:9000 http://localhost:4040;
                img-src 'self' data:;
                connect-src 'self' http://localhost:9080 ws://localhost:5173 http://localhost:9000 http://localhost:4040;
                form-action 'self';
                base-uri 'self'
              `.replace(/\s+/g, " ");
              proxyRes.headers["x-frame-options"] = "SAMEORIGIN";
              proxyRes.headers["access-control-allow-credentials"] = "true";
              proxyRes.headers["access-control-allow-methods"] =
                "GET, POST, OPTIONS";
              proxyRes.headers["access-control-allow-headers"] = "*";
              /*
              if (proxyRes.headers.location) {
                proxyRes.headers.location = proxyRes.headers.location.replace(
                  "9080",
                  "5173",
                );
              }*/
            });
          },
        },
        "/realms": {
          target: "http://localhost:9080",
          changeOrigin: true,
          secure: false,
          ws: true,
          configure: (proxy) => {
            proxy.on("proxyRes", (proxyRes) => {
              // Add specific header for login-status-iframe
              if (proxyRes.url?.includes("login-status-iframe")) {
                proxyRes.headers["content-security-policy"] =
                  "frame-ancestors 'self' http://localhost:5173";
              }
              // Add WebSocket upgrade headers
              proxyRes.headers["Upgrade"] = "websocket";
              proxyRes.headers["Connection"] = "Upgrade";
              proxyRes.headers["content-security-policy"] = `
                default-src 'self';
                script-src 'self' 'unsafe-inline' 'unsafe-eval' http://localhost:5173 http://localhost:9080;
                style-src 'self' 'unsafe-inline';
                frame-src 'self' http://localhost:9080 http://localhost:5173;
                frame-ancestors 'self' http://localhost:9080 http://localhost:5173;
                img-src 'self' data:;
                connect-src 'self' http://localhost:9080 ws://localhost:5173;
                form-action 'self';
                base-uri 'self'
              `.replace(/\s+/g, " ");
              proxyRes.headers["x-frame-options"] = "SAMEORIGIN";
              proxyRes.headers["access-control-allow-origin"] =
                "http://localhost:5173, http://localhost:9080";
              proxyRes.headers["access-control-allow-credentials"] = "true";
              proxyRes.headers["access-control-allow-methods"] =
                "GET, POST, OPTIONS, HEAD";
              proxyRes.headers["access-control-allow-headers"] =
                "Origin, X-Requested-With, Content-Type, Accept, Authorization";
              if (proxyRes.headers.location) {
                proxyRes.headers.location = proxyRes.headers.location.replace(
                  "9080",
                  "5173",
                );
              }
            });
          },
        },
        "/resources": {
          target: "http://localhost:9080",
          changeOrigin: true,
          secure: false,
          configure: (proxy) => {
            proxy.on("proxyRes", (proxyRes) => {
              proxyRes.headers["content-security-policy"] = `
                default-src 'self';
                script-src 'self' 'unsafe-inline' 'unsafe-eval' http://localhost:5173 http://localhost:9080;
                style-src 'self' 'unsafe-inline';
                frame-src 'self' http://localhost:9080 http://localhost:5173;
                frame-ancestors 'self' http://localhost:9080 http://localhost:5173;
                img-src 'self' data:;
                connect-src 'self' http://localhost:9080 ws://localhost:5173;
                form-action 'self';
                base-uri 'self'
              `.replace(/\s+/g, " ");
              proxyRes.headers["x-frame-options"] = "SAMEORIGIN";
              proxyRes.headers["access-control-allow-origin"] =
                "http://localhost:5173, http://localhost:9080";
              proxyRes.headers["access-control-allow-credentials"] = "true";
              proxyRes.headers["access-control-allow-methods"] =
                "GET, POST, OPTIONS";
              proxyRes.headers["access-control-allow-headers"] = "*";
              if (proxyRes.headers.location) {
                proxyRes.headers.location = proxyRes.headers.location.replace(
                  "9080",
                  "5173",
                );
              }
            });
          },
        },
      },
    },
    build: {
      ...lib,
      sourcemap: true,
      target: "esnext",
      modulePreload: false,
      cssMinify: "lightningcss",
      manifest: true,
      rollupOptions: {
        input,
        external,
      },
    },
    plugins,
    test: {
      watch: true,
      environment: "jsdom",
      server: {
        deps: {
          inline: [/@patternfly\/.*/],
        },
      },
    },
  };
});

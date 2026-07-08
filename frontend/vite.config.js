import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";
import { transformSync } from "esbuild";

// Components in this project use the .js extension (not .jsx) even though
// they contain JSX, matching the original project's file naming convention.
// Rollup's import-analysis step can't parse raw JSX, and esbuild only
// treats .jsx/.tsx as JSX by default, so this plugin transforms any .js
// file under src/ with esbuild (loader: "jsx") before anything else in the
// pipeline sees it.
const jsxInJsPlugin = () => ({
    name: "jsx-in-js",
    enforce: "pre",
    transform(code, id) {
        if (!/\/src\/.*\.js$/.test(id)) return null;

        const result = transformSync(code, {
            loader: "jsx",
            jsx: "automatic",
            sourcemap: true,
            sourcefile: id,
        });

        return {
            code: result.code,
            map: result.map,
        };
    },
});

export default defineConfig({
    plugins: [jsxInJsPlugin(), react()],
    // The dependency pre-bundling scanner runs its own separate esbuild
    // pass (outside the plugin pipeline above) to discover imports, so it
    // needs the same .js-as-jsx loader told to it directly.
    optimizeDeps: {
        esbuildOptions: {
            loader: {
                ".js": "jsx",
            },
        },
    },
    server: {
        port: 5173,
        proxy: {
            "/api": {
                target: process.env.VITE_API_PROXY_TARGET || "http://localhost:8080",
                changeOrigin: true,
            },
        },
    },
});

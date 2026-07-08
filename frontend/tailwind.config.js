/** @type {import('tailwindcss').Config} */
export default {
    content: ["./index.html", "./src/**/*.{js,jsx}"],
    theme: {
        extend: {
            colors: {
                ink: {
                    900: "#14181f",
                    800: "#1b2430",
                    700: "#2a3444",
                    500: "#5b6577",
                    300: "#99a1ad",
                },
                paper: {
                    DEFAULT: "#f2f1ec",
                    100: "#e9e7de",
                    200: "#dcd8c9",
                },
                amber: {
                    600: "#b5791f",
                    500: "#c98a2c",
                    100: "#f3e1bd",
                },
                forest: {
                    600: "#3f7d5c",
                    100: "#dcece2",
                },
                rust: {
                    600: "#a83b2b",
                    100: "#f3ddd8",
                },
                line: "#cfc9b8",
            },
            fontFamily: {
                mono: ["IBM Plex Mono", "monospace"],
                sans: ["IBM Plex Sans", "sans-serif"],
            },
        },
    },
    plugins: [],
};

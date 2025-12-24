export default {
    darkMode: ["class"],
    content: [
        "./index.html",
        "./src/**/*.{ts,tsx,js,jsx}",
        "./components/**/*.{ts,tsx,js,jsx}",
        "./app/**/*.{ts,tsx,js,jsx}",
    ],
    theme: {
        extend: {
            colors: {
                "ncsu-color": "#CC0000",
            },
            // shadcn typically also uses CSS vars for semantic colors; keep this simple for now
        },
    },
    plugins: [require("tailwindcss-animate")],
};

import {
    createContext,
    useContext,
    useEffect,
    useMemo,
    useState,
} from "react";

const THEME_STORAGE_KEY = "wolf-cafe-theme";

const ThemeProviderContext = createContext({
    theme: "system",
    setTheme: () => null,
});

export function ThemeProvider({
    children,
    defaultTheme = "system",
    storageKey = THEME_STORAGE_KEY,
}) {
    const [theme, setThemeState] = useState(() => {
        if (typeof window === "undefined") {
            return defaultTheme;
        }
        return localStorage.getItem(storageKey) || defaultTheme;
    });

    useEffect(() => {
        if (typeof window === "undefined") {
            return;
        }

        const root = window.document.documentElement;

        const applyTheme = (nextTheme) => {
            root.classList.remove("light", "dark");
            root.classList.add(nextTheme);
        };

        if (theme === "system") {
            const media = window.matchMedia("(prefers-color-scheme: dark)");
            const listener = (event) => {
                applyTheme(event.matches ? "dark" : "light");
            };

            applyTheme(media.matches ? "dark" : "light");
            media.addEventListener("change", listener);

            return () => media.removeEventListener("change", listener);
        }

        applyTheme(theme);
    }, [theme]);

    useEffect(() => {
        if (typeof window === "undefined" || theme === undefined) {
            return;
        }
        window.localStorage.setItem(storageKey, theme);
    }, [theme, storageKey]);

    const value = useMemo(
        () => ({
            theme,
            setTheme: (newTheme) => {
                setThemeState(newTheme);
            },
        }),
        [theme],
    );

    return (
        <ThemeProviderContext.Provider value={value}>
            {children}
        </ThemeProviderContext.Provider>
    );
}

export function useTheme() {
    return useContext(ThemeProviderContext);
}

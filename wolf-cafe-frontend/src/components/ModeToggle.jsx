import { Moon, Sun } from "lucide-react";

import { Button } from "@/components/ui/button";
import { useTheme } from "@/components/theme-provider";

const THEME_LABEL = {
    light: "Switch to dark mode",
    dark: "Switch to light mode",
    system: "Switch to dark mode",
};

export function ModeToggle() {
    const { theme = "system", setTheme } = useTheme();

    const handleToggle = () => {
        if (theme === "dark") {
            setTheme("light");
        } else {
            setTheme("dark");
        }
    };

    return (
        <Button
            type="button"
            variant="ghost"
            size="icon-sm"
            onClick={handleToggle}
            aria-label="Toggle theme"
            title={THEME_LABEL[theme]}
            className="relative text-foreground"
        >
            <Sun className="h-[1.2rem] w-[1.2rem] rotate-0 scale-100 transition-all dark:-rotate-90 dark:scale-0" />
            <Moon className="absolute h-[1.2rem] w-[1.2rem] rotate-90 scale-0 transition-all dark:rotate-0 dark:scale-100" />
        </Button>
    );
}

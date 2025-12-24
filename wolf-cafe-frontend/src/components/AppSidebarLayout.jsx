import {
    Sidebar,
    SidebarContent,
    SidebarFooter,
    SidebarGroup,
    SidebarGroupContent,
    SidebarHeader,
    SidebarInset,
    SidebarMenu,
    SidebarMenuButton,
    SidebarMenuItem,
    SidebarProvider,
    SidebarTrigger,
} from "@/components/ui/sidebar";
import {Button} from "@/components/ui/button";
import {logout} from "@/services/AuthService";
import React from "react";
import {NavLink, Outlet, useLocation, useNavigate} from "react-router-dom";
import {Boxes, ClipboardList, LogOut, Percent, ReceiptText, ShoppingCart, Users,} from "lucide-react";
import {ModeToggle} from "@/components/ModeToggle";
import {HOME_ROUTE, isRoleOneOf, normalizeRole, ROLES,} from "@/constants/roles";

const NAV_ITEMS = [
    {
        label: "Place Order",
        to: "/place-order",
        icon: ShoppingCart,
        roles: [ROLES.CUSTOMER, ROLES.STAFF, ROLES.ADMIN],
    },
    {
        label: "Your Orders",
        to: "/your-orders",
        icon: ReceiptText,
        roles: [ROLES.CUSTOMER, ROLES.STAFF, ROLES.ADMIN]
    },
    {
        label: "Orders",
        to: "/orders",
        icon: ClipboardList,
        roles: [ROLES.STAFF, ROLES.ADMIN],
    },
    {
        label: "Inventory",
        to: "/items",
        icon: Boxes,
        roles: [ROLES.STAFF, ROLES.ADMIN],
    },
    {
        label: "Users",
        to: "/users",
        icon: Users,
        roles: [ROLES.ADMIN],
    },
    {
        label: "Tax Rate",
        to: "/tax-rates",
        icon: Percent,
        roles: [ROLES.ADMIN],
    },
];

const AppSidebarLayout = () => {
    const location = useLocation();
    const navigate = useNavigate();
    const [userDisplayName, setUserDisplayName] = React.useState("");
    const [userRole, setUserRole] = React.useState("");

    React.useEffect(() => {
        const storedUsername =
            sessionStorage.getItem("authenticatedUser") || "";
        const storedRole = sessionStorage.getItem("role") || "";

        setUserDisplayName(storedUsername);
        setUserRole(storedRole);
    }, []);

    const formattedRole = React.useMemo(() => {
        if (!userRole) return "";
        return userRole
            .replace(/^ROLE_/i, "")
            .toLowerCase()
            .split(" ")
            .map((part) => part.charAt(0).toUpperCase() + part.slice(1))
            .join(" ");
    }, [userRole]);

    const normalizedRole = React.useMemo(
        () => normalizeRole(userRole),
        [userRole],
    );

    const accessibleNavItems = React.useMemo(() => {
        return NAV_ITEMS.filter((item) =>
            isRoleOneOf(normalizedRole, item.roles),
        );
    }, [normalizedRole]);

    const activeLabel = React.useMemo(() => {
        const current = accessibleNavItems.find((item) =>
            location.pathname.startsWith(item.to),
        );
        return current?.label ?? "WolfCafe";
    }, [accessibleNavItems, location.pathname]);

    const handleNavigateHome = React.useCallback(() => {
        navigate(HOME_ROUTE);
    }, [navigate]);

    function handleLogout() {
        logout();
        navigate("/login");
    }

    return (
        <SidebarProvider>
            <div className="h-dvh w-full bg-background overflow-hidden">
                <div className="flex h-full">
                    <Sidebar
                        collapsible="icon"
                        className="overscroll-contain overflow-hidden"
                    >
                        <SidebarHeader>
                            <button
                                type="button"
                                onClick={handleNavigateHome}
                                className="flex w-full items-center gap-2 px-2 py-1 text-left focus:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2"
                            >
                                <img
                                    src="/wolf-head.png"
                                    alt="WolfCafe Logo"
                                    className="h-8 w-8 shrink-0 object-contain"
                                />
                                <span
                                    className="text-lg font-semibold text-primary group-data-[state=collapsed]:hidden">
                                    WolfCafe
                                </span>
                            </button>
                        </SidebarHeader>
                        <SidebarContent>
                            <SidebarGroup>
                                <SidebarGroupContent>
                                    <SidebarMenu>
                                        {accessibleNavItems.map((item) => {
                                            const Icon = item.icon;
                                            const isActive =
                                                location.pathname === item.to ||
                                                location.pathname.startsWith(
                                                    `${item.to}/`,
                                                );

                                            return (
                                                <SidebarMenuItem key={item.to}>
                                                    <SidebarMenuButton
                                                        asChild
                                                        isActive={isActive}
                                                    >
                                                        <NavLink to={item.to}>
                                                            {Icon && (
                                                                <Icon className="h-4 w-4"/>
                                                            )}
                                                            <span>
                                                                {item.label}
                                                            </span>
                                                        </NavLink>
                                                    </SidebarMenuButton>
                                                </SidebarMenuItem>
                                            );
                                        })}
                                    </SidebarMenu>
                                </SidebarGroupContent>
                            </SidebarGroup>
                        </SidebarContent>
                        <SidebarFooter>
                            <div
                                className="flex items-center justify-between gap-3 px-2 py-2 group-data-[state=collapsed]:justify-center">
                                <div className="flex flex-col leading-tight group-data-[state=collapsed]:hidden">
                                    <span className="text-sm font-medium text-sidebar-foreground">
                                        {userDisplayName || "User"}
                                    </span>
                                    {formattedRole && (
                                        <span className="text-xs text-muted-foreground">
                                            {formattedRole}
                                        </span>
                                    )}
                                </div>
                                <Button
                                    variant="ghost"
                                    size="icon-lg"
                                    onClick={handleLogout}
                                    aria-label="Log out"
                                >
                                    <LogOut className="h-5 w-5"/>
                                </Button>
                            </div>
                        </SidebarFooter>
                    </Sidebar>
                    <SidebarInset className="grid flex-1 grid-rows-[auto,1fr] min-w-0 overflow-hidden">
                        <header
                            className="row-start-1 row-end-2 flex h-14 items-center justify-between border-b px-4 bg-background">
                            <div className="flex items-center gap-2">
                                <SidebarTrigger/>
                                <h1 className="text-lg font-medium">
                                    {activeLabel}
                                </h1>
                            </div>
                            <ModeToggle/>
                        </header>
                        <div
                            className="row-start-2 row-end-3 min-h-0 overflow-y-auto overscroll-contain p-0 [-webkit-overflow-scrolling:touch]">
                            <div className="flex min-h-[calc(100vh-3.5rem)] flex-col">
                                <Outlet/>
                            </div>
                        </div>
                    </SidebarInset>
                </div>
            </div>
        </SidebarProvider>
    );
};

export default AppSidebarLayout;

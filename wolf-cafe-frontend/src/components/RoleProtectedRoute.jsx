import React from "react";
import { Navigate } from "react-router-dom";
import {
    getHomeRouteForRole,
    isRoleOneOf,
    normalizeRole,
} from "@/constants/roles";
import { isUserLoggedIn } from "@/services/AuthService";

const RoleProtectedRoute = ({ allowedRoles = [], children }) => {
    const [role, setRole] = React.useState(() => {
        if (typeof window === "undefined") return "";
        return sessionStorage.getItem("role") || "";
    });

    React.useEffect(() => {
        if (typeof window === "undefined") return;
        setRole(sessionStorage.getItem("role") || "");
    }, []);

    if (!isUserLoggedIn()) {
        return <Navigate to="/login" replace />;
    }

    const normalizedRole = normalizeRole(role);
    if (!isRoleOneOf(normalizedRole, allowedRoles)) {
        const fallbackPath = getHomeRouteForRole(normalizedRole);
        return <Navigate to={fallbackPath} replace />;
    }

    return children;
};

export default RoleProtectedRoute;

export const ROLES = {
    CUSTOMER: "ROLE_CUSTOMER",
    STAFF: "ROLE_STAFF",
    ADMIN: "ROLE_ADMIN",
};

export const HOME_ROUTE = "/";

export function normalizeRole(role) {
    if (!role) return "";
    const upperRole = role.toUpperCase();
    if (upperRole.startsWith("ROLE_")) {
        return upperRole;
    }
    return `ROLE_${upperRole}`;
}

export function isRoleOneOf(role, allowedRoles = []) {
    if (!allowedRoles.length) return true;
    const normalizedRole = normalizeRole(role);
    return allowedRoles.map(normalizeRole).includes(normalizedRole);
}

export function getHomeRouteForRole() {
    return HOME_ROUTE;
}

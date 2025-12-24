import React from "react";
import AdminHomeComponent from "./AdminHomeComponent";
import StaffHomeComponent from "./StaffHomeComponent";
import CustomerHomeComponent from "./CustomerHomeComponent";
import { ROLES, normalizeRole } from "@/constants/roles";

const HomeComponent = () => {
    const [role, setRole] = React.useState(() => {
        if (typeof window === "undefined") return "";
        return sessionStorage.getItem("role") || "";
    });

    React.useEffect(() => {
        if (typeof window === "undefined") return;
        setRole(sessionStorage.getItem("role") || "");
    }, []);

    const normalizedRole = normalizeRole(role);

    switch (normalizedRole) {
        case ROLES.ADMIN:
            return <AdminHomeComponent />;
        case ROLES.STAFF:
            return <StaffHomeComponent />;
        case ROLES.CUSTOMER:
            return <CustomerHomeComponent />;
        default:
            return (
                <div className="p-6">
                    <p className="text-sm text-muted-foreground">
                        Unable to determine your role. Please sign in again.
                    </p>
                </div>
            );
    }
};

export default HomeComponent;

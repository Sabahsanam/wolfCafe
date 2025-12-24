import React, {useEffect, useMemo, useState} from "react";
import {Card, CardContent, CardFooter} from "@/components/ui/card";
import {Button} from "@/components/ui/button";
import {Alert, AlertDescription, AlertTitle} from "@/components/ui/alert";
import {AlertCircle, CheckCircle2, Trash2, UserPlus, XCircle,} from "lucide-react";

import {createUser, deleteUser, getRoles, getUsers, updateUser,} from "../services/UsersService";
import {Input} from "@/components/ui/input.tsx";
import {Label} from "@/components/ui/label.tsx";
import {Spinner} from "@/components/ui/spinner";

import {Select, SelectContent, SelectItem, SelectTrigger, SelectValue,} from "@/components/ui/select";

const UsersComponent = () => {
    const headers = useMemo(
        () => ["Name", "Role", "Username", "Actions"],
        []
    );
    const userColumnTemplate = "minmax(220px,2fr) minmax(140px,1fr) minmax(160px,1fr) minmax(160px,0.9fr)";

    const [users, setUsers] = useState([]);
    const [roles, setRoles] = useState([]);

    const [loadingUsers, setLoadingUsers] = useState(true);
    const [loadingRoles, setLoadingRoles] = useState(true);
    const [loadError, setLoadError] = useState(null);

    const [form, setForm] = useState({
        name: "",
        username: "",
        email: "",
        password: "",
        confirmPassword: "",
        role: "",
    });

    // "create" | "edit"
    const [mode, setMode] = useState("create");
    const [selectedUserId, setSelectedUserId] = useState(null);

    const [formError, setFormError] = useState(null);
    const [formSuccess, setFormSuccess] = useState(null);
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [deletingId, setDeletingId] = useState(null);

    // ---------- helpers for user data ----------

    const getUserId = (user) =>
        user?.id ?? null;

    const getUserName = (user) =>
        user?.name ??
        user?.fullName ??
        user?.displayName ??
        user?.username ??
        "";

    const getUserUsername = (user) =>
        user?.username ?? user?.login ?? user?.email ?? "";

    const getUserPrimaryRoleValue = (user) => {
        if (Array.isArray(user?.roles) && user.roles.length > 0) {
            return user.roles[0];
        }
        if (typeof user?.role === "string") {
            return user.role;
        }
        return "";
    };

    const getUserPrimaryRoleLabel = (user) => {
        const raw = getUserPrimaryRoleValue(user);
        if (!raw) return "";
        return String(raw).replace(/^ROLE_/, "").toUpperCase();
    };

    const roleBadgeClass = (role) => {
        const label = String(role || "").toUpperCase();
        const base =
            "inline-flex items-center rounded-full px-3 py-1 text-[11px] font-semibold border shadow-[0_1px_0_rgba(0,0,0,0.04)]";

        if (!label) {
            return `${base} border-slate-300 bg-slate-50 text-slate-700 dark:border-slate-600 dark:bg-slate-900/50 dark:text-slate-100`;
        }
        if (label.includes("ADMIN")) {
            return `${base} border-red-300 bg-red-50 text-red-800 dark:border-red-700 dark:bg-red-900/60 dark:text-red-50`;
        }
        if (label.includes("STAFF") || label.includes("MANAGER")) {
            return `${base} border-sky-300 bg-sky-50 text-sky-800 dark:border-sky-700 dark:bg-sky-900/60 dark:text-sky-50`;
        }
        return `${base} border-amber-300 bg-amber-50 text-amber-800 dark:border-amber-700 dark:bg-amber-900/60 dark:text-amber-50`;
    };

    // ---------- backend loaders ----------

    const loadUsers = async () => {
        setLoadingUsers(true);
        setLoadError(null);
        try {
            const res = await getUsers();
            const list = Array.isArray(res?.data) ? res.data : res ?? [];
            setUsers(list);
        } catch (e) {
            console.error("Failed to load users", e);
            setUsers([]);
            setLoadError(
                e?.response?.data || e?.message || "Failed to load users."
            );
        } finally {
            setLoadingUsers(false);
        }
    };

    const loadRoles = async () => {
        setLoadingRoles(true);
        try {
            const res = await getRoles();
            const list = Array.isArray(res?.data) ? res.data : [];
            setRoles(list);
        } catch (e) {
            console.error("Failed to load roles", e);
            setRoles([]);
        } finally {
            setLoadingRoles(false);
        }
    };

    useEffect(() => {
        loadUsers();
        loadRoles();
    }, []);

    // ---------- form helpers ----------

    const resetForm = () => {
        setForm({
            name: "",
            username: "",
            email: "",
            password: "",
            confirmPassword: "",
            role: "",
        });
        setFormError(null);
        // DO NOT clear formSuccess here, so the green success banner
        // can stay visible after successful operations.
    };

    const switchToCreateMode = () => {
        setMode("create");
        setSelectedUserId(null);
        resetForm();
    };

    const handleInputChange = (e) => {
        const {name, value} = e.target;
        setForm((prev) => ({
            ...prev,
            [name]: value,
        }));
        setFormError(null);
        setFormSuccess(null);
    };

    const handleSelectUserForEdit = (user) => {
        const id = getUserId(user);
        if (!id) return;

        setMode("edit");
        setSelectedUserId(id);
        setForm({
            name: getUserName(user),
            username: getUserUsername(user),
            email: user?.email || "",
            password: "",
            confirmPassword: "",
            role: getUserPrimaryRoleValue(user) || "",
        });
        setFormError(null);
        setFormSuccess(null);
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setFormError(null);
        setFormSuccess(null);

        const name = form.name.trim();
        const username = form.username.trim();
        const email = form.email.trim();
        const password = form.password.trim();
        const confirmPassword = form.confirmPassword.trim();
        const role = form.role; // as returned from /api/roles, e.g. "ROLE_ADMIN"

        if (!name || !username) {
            setFormError("Name and username are required.");
            return;
        }

        if (mode === "create") {
            if (!password || !confirmPassword) {
                setFormError("Password and confirmation are required for new users.");
                return;
            }
            if (password !== confirmPassword) {
                setFormError("Password and confirmation do not match.");
                return;
            }

            setIsSubmitting(true);
            try {
                // 1) create the user (backend will set default role, e.g., CUSTOMER)
                const res = await createUser({name, username, email, password});
                const created = res?.data;

                // 2) If an explicit role was selected, and we got an id back, update roles
                if (role && created && created.id) {
                    const payload = {name, username, email, roles: [role]};
                    await updateUser(created.id, payload);
                }

                setFormSuccess("User created successfully.");
                switchToCreateMode();
                await loadUsers();
            } catch (err) {
                console.error("Failed to create user", err);
                setFormError(
                    err?.response?.data ||
                    err?.message ||
                    "Failed to create user. Please try again."
                );
            } finally {
                setIsSubmitting(false);
            }
        } else {
            // EDIT mode
            if (!selectedUserId) {
                setFormError("No user selected for editing.");
                return;
            }

            setIsSubmitting(true);
            try {
                const payload = {
                    name,
                    username,
                    email,
                    roles: role ? [role] : null,
                };

                await updateUser(selectedUserId, payload);
                setFormSuccess("User updated successfully.");
                await loadUsers();
            } catch (err) {
                console.error("Failed to update user", err);
                setFormError(
                    err?.response?.data ||
                    err?.message ||
                    "Failed to update user. Please try again."
                );
            } finally {
                setIsSubmitting(false);
            }
        }
    };

    const handleDeleteUser = async (user) => {
        const id = getUserId(user);
        const username = getUserUsername(user);
        const roleLabel = getUserPrimaryRoleLabel(user);

        // Guard: never delete ADMIN users
        if (roleLabel && roleLabel.toUpperCase().includes("ADMIN")) {
            setFormError("You cannot delete a user with the ADMIN role.");
            return;
        }

        if (!id) {
            setFormError("Cannot delete user: missing user id from backend.");
            return;
        }

        const confirmed = window.confirm(
            `Are you sure you want to delete user "${username}"?`
        );
        if (!confirmed) return;

        setDeletingId(id);
        setFormError(null);
        setFormSuccess(null);

        try {
            await deleteUser(id);

            // If we deleted the user currently being edited, reset form
            if (mode === "edit" && selectedUserId === id) {
                switchToCreateMode();
            }

            await loadUsers();
            setFormSuccess(`User "${username}" deleted successfully.`);
        } catch (err) {
            console.error("Failed to delete user", err);
            setFormError(
                err?.response?.data ||
                err?.message ||
                "Failed to delete user. Please try again."
            );
        } finally {
            setDeletingId(null);
        }
    };

    const hasUsers = users && users.length > 0;

    return (
        <div className="min-h-full bg-background px-5 py-6 md:p-8 overscroll-contain">
            <div className="mx-auto space-y-2">


                <div className="flex flex-wrap gap-6 xl:flex-nowrap">
                    {/* Users list */}
                    <div className="flex flex-col gap-3 flex-1 min-w-[320px] max-w-3xl">
                        <h2 className="text-xl font-medium">Users</h2>
                        <Card className="overflow-hidden border shadow-sm p-0 max-w-[720px]">
                            <div className="overflow-x-auto">
                                <div className="inline-block min-w-full">
                                    {/* Header */}
                                    <div
                                        className="grid bg-muted/80 dark:bg-muted/60 border-b-2 border-border"
                                        style={{gridTemplateColumns: userColumnTemplate}}
                                    >
                                        {headers.map((header, index) => (
                                            <div
                                                key={`header-${index}`}
                                                className="border-r border-border h-11 font-semibold flex items-center px-4 text-foreground/90 text-sm tracking-wide"
                                            >
                                                {header}
                                            </div>
                                        ))}
                                    </div>

                                    {/* Body */}
                                    <div className="h-[600px] overflow-y-auto">
                                        <div className="flex flex-col">
                                            {loadingUsers && (
                                                <div
                                                    className="col-span-4 border-b border-border h-12 flex items-center px-4 text-sm text-muted-foreground gap-2">
                                                    <Spinner className="h-4 w-4"/>
                                                    Loading users...
                                                </div>
                                            )}

                                            {!loadingUsers &&
                                                !hasUsers &&
                                                !loadError && (
                                                    <div
                                                        className="col-span-4 border-b border-border h-12 flex items-center px-4 text-sm text-muted-foreground italic">
                                                        No users found.
                                                    </div>
                                                )}

                                            {!loadingUsers &&
                                                hasUsers &&
                                                users.map((user) => {
                                                    const id = getUserId(user);
                                                    const name = getUserName(user);
                                                    const username =
                                                        getUserUsername(user);
                                                    const roleLabel =
                                                        getUserPrimaryRoleLabel(user);
                                                    const isSelected =
                                                        selectedUserId === id;

                                                    return (
                                                        <div
                                                            key={
                                                                id ??
                                                                username ??
                                                                name
                                                            }
                                                            role="button"
                                                            tabIndex={0}
                                                            onClick={() =>
                                                                handleSelectUserForEdit(
                                                                    user
                                                                )
                                                            }
                                                            onKeyDown={(e) => {
                                                                if (
                                                                    e.key ===
                                                                    "Enter" ||
                                                                    e.key === " "
                                                                ) {
                                                                    e.preventDefault();
                                                                    handleSelectUserForEdit(
                                                                        user
                                                                    );
                                                                }
                                                            }}
                                                            className={`grid border-b border-border transition-colors ${
                                                                isSelected
                                                                    ? "bg-muted/60 dark:bg-muted/40"
                                                                    : "hover:bg-muted/40 dark:hover:bg-muted/30"
                                                            }`}
                                                            style={{gridTemplateColumns: userColumnTemplate}}
                                                        >
                                                            <div
                                                                className="border-r border-border h-12 flex items-center px-4">
                                                                <span className="text-sm text-foreground">
                                                                    {name ||
                                                                        "(unnamed user)"}
                                                                </span>
                                                            </div>

                                                            <div
                                                                className="border-r border-border h-12 flex items-center px-4">
                                                                <span
                                                                    className={roleBadgeClass(
                                                                        roleLabel
                                                                    )}
                                                                >
                                                                    {roleLabel ||
                                                                        "—"}
                                                                </span>
                                                            </div>

                                                            <div
                                                                className="border-r border-border h-12 flex items-center px-4">
                                                                <span className="text-sm text-foreground">
                                                                    {username ||
                                                                        "—"}
                                                                </span>
                                                            </div>

                                                            <div
                                                                className="border-border flex items-center px-4 py-2 w-full">
                                                                <div
                                                                    className="flex w-full max-w-[220px] mx-auto items-center justify-center gap-3 px-2">
                                                                    <Button
                                                                        type="button"
                                                                        size="sm"
                                                                        variant="destructive"
                                                                        className="h-8 px-3 text-xs"
                                                                        onClick={(e) => {
                                                                            e.stopPropagation();
                                                                            handleDeleteUser(
                                                                                user
                                                                            );
                                                                        }}
                                                                        disabled={
                                                                            deletingId ===
                                                                            id ||
                                                                            loadingUsers
                                                                        }
                                                                    >
                                                                        {deletingId ===
                                                                        id ? (
                                                                            <Spinner className="h-3.5 w-3.5"/>
                                                                        ) : (
                                                                            <Trash2 className="h-3 w-3 mr-1"/>
                                                                        )}
                                                                        Delete
                                                                    </Button>
                                                                </div>
                                                            </div>
                                                        </div>
                                                    );
                                                })}

                                            {loadError && (
                                                <div className="col-span-4 border-b border-border h-auto px-4 py-3">
                                                    <Alert variant="destructive">
                                                        <AlertCircle className="h-4 w-4"/>
                                                        <AlertTitle>
                                                            Failed to load users
                                                        </AlertTitle>
                                                        <AlertDescription>
                                                            {String(loadError)}
                                                        </AlertDescription>
                                                    </Alert>
                                                </div>
                                            )}
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </Card>

                    </div>

                    {/* New / Edit user panel (right) */}
                    <div className="w-full xl:w-80 flex flex-col gap-3">
                        <div className="flex items-center justify-between gap-2">
                            <h2 className="text-xl font-semibold">
                                {mode === "create"
                                    ? "Add User"
                                    : "Edit User"}
                            </h2>
                            {mode === "edit" && (
                                <Button
                                    type="button"
                                    variant="ghost"
                                    size="icon"
                                    className="h-7 w-7 text-muted-foreground hover:text-foreground"
                                    onClick={switchToCreateMode}
                                    title="Cancel edit"
                                >
                                    <XCircle className="h-4 w-4"/>
                                </Button>
                            )}
                        </div>

                        <Card className="border shadow-sm">
                            <CardContent className="pt-2">
                                <form className="space-y-4" onSubmit={handleSubmit}>
                                    <div className="space-y-2">
                                        <Label
                                            htmlFor="name"
                                            className="text-xs font-medium tracking-wide text-muted-foreground"
                                        >
                                            Name
                                        </Label>
                                        <Input
                                            id="name"
                                            type="text"
                                            name="name"
                                            value={form.name}
                                            onChange={handleInputChange}
                                            placeholder="e.g. Jane Smith"
                                            autoComplete="off"
                                        />
                                    </div>

                                    <div className="space-y-2">
                                        <Label
                                            htmlFor="username"
                                            className="text-xs font-medium tracking-wide text-muted-foreground"
                                        >
                                            Username
                                        </Label>
                                        <Input
                                            id="username"
                                            type="text"
                                            name="username"
                                            value={form.username}
                                            onChange={handleInputChange}
                                            placeholder="jsmith"
                                            autoComplete="off"
                                        />
                                    </div>

                                    <div className="space-y-2">
                                        <Label
                                            htmlFor="email"
                                            className="text-xs font-medium tracking-wide text-muted-foreground"
                                        >
                                            Email
                                        </Label>
                                        <Input
                                            id="email"
                                            type="email"
                                            name="email"
                                            value={form.email}
                                            onChange={handleInputChange}
                                            placeholder="jsmith@example.com"
                                            autoComplete="off"
                                        />
                                    </div>

                                    {mode === "create" && (
                                        <>
                                            <div className="space-y-2">
                                                <Label
                                                    htmlFor="password"
                                                    className="text-xs font-medium tracking-wide text-muted-foreground"
                                                >
                                                    Password
                                                </Label>
                                                <Input
                                                    id="password"
                                                    type="password"
                                                    name="password"
                                                    value={form.password}
                                                    onChange={handleInputChange}
                                                    placeholder="••••••••"
                                                    autoComplete="new-password"
                                                />
                                            </div>

                                            <div className="space-y-2">
                                                <Label
                                                    htmlFor="confirmPassword"
                                                    className="text-xs font-medium tracking-wide text-muted-foreground"
                                                >
                                                    Confirm Password
                                                </Label>
                                                <Input
                                                    id="confirmPassword"
                                                    type="password"
                                                    name="confirmPassword"
                                                    value={form.confirmPassword}
                                                    onChange={handleInputChange}
                                                    placeholder="Repeat password"
                                                    autoComplete="new-password"
                                                />
                                            </div>
                                        </>
                                    )}

                                    <div className="space-y-2">
                                        <Label
                                            htmlFor="role"
                                            className="text-xs font-medium tracking-wide text-muted-foreground"
                                        >
                                            Role (optional)
                                        </Label>
                                        <Select
                                            value={form.role}
                                            onValueChange={(value) =>
                                                handleInputChange({target: {name: "role", value}})
                                            }
                                            disabled={loadingRoles}
                                        >
                                            <SelectTrigger className="h-9 w-full">
                                                <SelectValue
                                                    placeholder={
                                                        loadingRoles ? "Loading roles..." : "Use backend default role"
                                                    }
                                                />
                                            </SelectTrigger>

                                            <SelectContent>
                                                {roles.map((r) => {
                                                    const label = String(r).replace(/^ROLE_/, "").toUpperCase();
                                                    return (
                                                        <SelectItem key={r} value={r}>
                                                            {label}
                                                        </SelectItem>
                                                    );
                                                })}
                                            </SelectContent>
                                        </Select>
                                    </div>

                                    <div className="pt-2 space-y-2">
                                        <Button
                                            type="submit"
                                            size="lg"
                                            className="w-full h-11 text-sm"
                                            disabled={isSubmitting}
                                        >
                                            {isSubmitting ? (
                                                mode === "create"
                                                    ? "Creating..."
                                                    : "Saving..."
                                            ) : mode === "create" ? (
                                                <>
                                                    <UserPlus className="h-4 w-4 mr-2"/>
                                                    Add New User
                                                </>
                                            ) : (
                                                "Save Changes"
                                            )}
                                        </Button>

                                        <Button
                                            type="button"
                                            variant="outline"
                                            className="w-full h-10 text-xs bg-transparent"
                                            onClick={
                                                mode === "create"
                                                    ? resetForm
                                                    : switchToCreateMode
                                            }
                                            disabled={isSubmitting}
                                        >
                                            {mode === "create"
                                                ? "Clear Form"
                                                : "Cancel Edit"}
                                        </Button>
                                    </div>
                                </form>
                            </CardContent>

                            {(formError || formSuccess) && (
                                <CardFooter className="flex flex-col gap-2 border-t border-border pt-4">
                                    {formError && (
                                        <Alert variant="destructive" className="px-3 py-2 w-full">
                                            <AlertCircle className="h-4 w-4"/>
                                            <AlertTitle className="text-sm">
                                                Cannot save changes
                                            </AlertTitle>
                                            <AlertDescription className="text-xs">
                                                {String(formError)}
                                            </AlertDescription>
                                        </Alert>
                                    )}

                                    {formSuccess && (
                                        <Alert
                                            className="px-3 py-2 w-full border border-green-300 bg-green-50 text-green-800 dark:border-green-800 dark:bg-green-900/30 dark:text-green-100">
                                            <CheckCircle2 className="h-4 w-4"/>
                                            <AlertTitle className="text-sm">
                                                Success
                                            </AlertTitle>
                                            <AlertDescription className="text-xs">
                                                {formSuccess}
                                            </AlertDescription>
                                        </Alert>
                                    )}
                                </CardFooter>
                            )}
                        </Card>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default UsersComponent;

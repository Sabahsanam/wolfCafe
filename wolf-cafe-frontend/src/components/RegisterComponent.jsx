import React, { useRef, useState } from "react";
import { registerAPICall } from "../services/AuthService";
import { useNavigate } from "react-router-dom";

import { Button } from "@/components/ui/button";
import { Eye, EyeClosed } from "lucide-react";
import {
    Card,
    CardAction,
    CardContent,
    CardDescription,
    CardFooter,
    CardHeader,
    CardTitle,
} from "@/components/ui/card";
import { Separator } from "@/components/ui/separator";
import { ModeToggle } from "@/components/ModeToggle";
import {
    InputGroup,
    InputGroupAddon,
    InputGroupButton,
    InputGroupInput,
    InputGroupText,
    InputGroupTextarea,
} from "@/components/ui/input-group";
import { Input } from "@/components/ui/input";

import {
    Field,
    FieldDescription,
    FieldGroup,
    FieldLabel,
    FieldLegend,
    FieldSeparator,
    FieldSet,
} from "@/components/ui/field";

const RegisterComponent = () => {
    const [name, setName] = useState("");
    const [username, setUsername] = useState("");
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [showPassword, setShowPassword] = useState(false);
    const [isLoading, setIsLoading] = useState(false);

    const [errorMessage, setErrorMessage] = useState("");
    const [successMessage, setSuccessMessage] = useState("");

    const submitGuardRef = useRef(false);     // prevents rapid double-click
    const reqIdRef = useRef(0);               // only latest request can set UI
    const navigator = useNavigate();

    async function handleRegistrationForm(e) {
        if (e && e.preventDefault) e.preventDefault();
        if (isLoading || submitGuardRef.current) return;

        const payload = { name, username, email, password };

        setErrorMessage("");
        setSuccessMessage("");
        setIsLoading(true);
        submitGuardRef.current = true;

        const myReqId = ++reqIdRef.current;

        try {
            const res = await registerAPICall(payload);

            if (reqIdRef.current === myReqId) {
                setSuccessMessage("Account created successfully. You can now sign in.");
                setErrorMessage("");
                setName("");
                setUsername("");
                setEmail("");
                setPassword("");

                // ADD THIS:
                setTimeout(() => {
                    navigator("/login");   // or navigator("/")
                }, 200); // ~1.2 seconds feels natural
            }
        } catch (error) {
            if (reqIdRef.current !== myReqId) return;

            console.error("Register error:", error);

            const hasResponse = !!error?.response;
            const status = error?.response?.status;
            const data = error?.response?.data;

            let backendMessage = null;

            if (hasResponse) {
                // Common Spring shapes: { message, details, ... } or { error, ... }
                if (typeof data === "string") {
                    backendMessage = data;
                } else if (data?.message) {
                    backendMessage = data.message;
                } else if (data?.error) {
                    backendMessage = data.error;
                } else if (Array.isArray(data?.errors) && data.errors.length > 0) {
                    backendMessage = data.errors[0];
                } else if (status === 409) {
                    backendMessage = "Username already exists.";
                }
            }

            let finalMessage = backendMessage;

            if (!finalMessage) {
                if (!hasResponse) {
                    finalMessage =
                        "Unable to reach server. Please check your connection or try again.";
                } else if (status >= 500) {
                    finalMessage =
                        "A server error occurred while registering. Please try again.";
                } else if (status >= 400) {
                    finalMessage =
                        "There was a problem with your registration details. Please review and try again.";
                } else {
                    finalMessage = "Error registering. Please try again.";
                }
            }

            setErrorMessage(finalMessage);
            setSuccessMessage("");
        } finally {
            if (reqIdRef.current === myReqId) {
                setIsLoading(false);
                setTimeout(() => {
                    submitGuardRef.current = false;
                }, 400);
            }
        }
    }


    return (
        <>
            <div className="fixed top-4 right-4 z-50 print:hidden">
                <ModeToggle />
            </div>
            <div className="bg-muted flex min-h-svh flex-col items-center justify-center gap-6 p-6 md:p-10 overflow-hidden overscroll-none">
                <Card className="w-full max-w-md rounded-lg border shadow-sm">
                    <CardHeader>
                        <div className="inline-flex items-baseline justify">
                            <img
                                src="/wolf-head.png"
                                alt="WolfCafe Logo"
                                className="h-8 mr-2 align-baseline"
                            />
                            <span className="text-xl font-medium text-primary">WolfCafe</span>
                        </div>
                    </CardHeader>

                    {/* Keep structure/visuals identical */}
                    <form onSubmit={handleRegistrationForm} className="flex flex-col gap-6" noValidate>
                        <CardContent>
                            <div>
                                <FieldSet>
                                    <FieldGroup className="gap-2">
                                        <Field>
                                            <FieldLabel htmlFor="name">Name</FieldLabel>
                                            <Input
                                                id="name"
                                                type="text"
                                                placeholder="Enter Name"
                                                value={name}
                                                onChange={(e) => {
                                                    setName(e.target.value);
                                                    if (errorMessage) setErrorMessage("");
                                                    if (successMessage) setSuccessMessage("");
                                                }}
                                                autoComplete="name"
                                            />
                                        </Field>
                                        <Field>
                                            <FieldLabel htmlFor="username">Username</FieldLabel>
                                            <Input
                                                id="username"
                                                type="text"
                                                placeholder="Enter Username"
                                                value={username}
                                                onChange={(e) => {
                                                    setUsername(e.target.value);
                                                    if (errorMessage) setErrorMessage("");
                                                    if (successMessage) setSuccessMessage("");
                                                }}
                                                autoComplete="username"
                                            />
                                        </Field>
                                        <Field>
                                            <FieldLabel htmlFor="email">Email</FieldLabel>
                                            <Input
                                                id="email"
                                                type="email"
                                                placeholder="Enter Email"
                                                value={email}
                                                onChange={(e) => {
                                                    setEmail(e.target.value);
                                                    if (errorMessage) setErrorMessage("");
                                                    if (successMessage) setSuccessMessage("");
                                                }}
                                                autoComplete="email"
                                            />
                                        </Field>
                                        <Field>
                                            <FieldLabel htmlFor="password">Password</FieldLabel>
                                            <InputGroup>
                                                <InputGroupInput
                                                    id="password"
                                                    type={showPassword ? "text" : "password"}
                                                    placeholder="Enter Password"
                                                    value={password}
                                                    onChange={(e) => {
                                                        setPassword(e.target.value);
                                                        if (errorMessage) setErrorMessage("");
                                                        if (successMessage) setSuccessMessage("");
                                                    }}
                                                    autoComplete="new-password"
                                                />
                                                <InputGroupAddon align="inline-end">
                                                    <InputGroupButton
                                                        size="icon-xs"
                                                        type="button" // important: don't submit
                                                        onClick={() => setShowPassword((prev) => !prev)}
                                                        aria-label={showPassword ? "Hide password" : "Show password"}
                                                    >
                                                        {showPassword ? <Eye /> : <EyeClosed />}
                                                    </InputGroupButton>
                                                </InputGroupAddon>
                                            </InputGroup>
                                        </Field>
                                    </FieldGroup>
                                </FieldSet>

                                {/* Messages */}
                                {errorMessage && (
                                    <p className="text-destructive text-sm mt-2" role="alert">
                                        {errorMessage}
                                    </p>
                                )}
                                {successMessage && (
                                    <p className="text-green-600 dark:text-green-400 text-sm mt-2" role="status">
                                        {successMessage}
                                    </p>
                                )}
                            </div>
                        </CardContent>

                        <CardFooter className="flex-col gap-2">
                            <Button type="submit" className="w-full" disabled={isLoading}>
                                {isLoading ? "Registering..." : "Register"}
                            </Button>
                        </CardFooter>
                    </form>
                </Card>
            </div>
        </>
    );
};

export default RegisterComponent;

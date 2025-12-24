import React, {useState} from "react";
import {loginAPICall, saveLoggedInUser, storeToken,} from "../services/AuthService";
import {useNavigate} from "react-router-dom";
import {Button} from "@/components/ui/button";
import {Eye, EyeClosed} from "lucide-react";
import {Card, CardContent, CardFooter, CardHeader,} from "@/components/ui/card";
import {ModeToggle} from "@/components/ModeToggle";

import {InputGroup, InputGroupAddon, InputGroupButton, InputGroupInput,} from "@/components/ui/input-group";
import {Input} from "@/components/ui/input";

import {Field, FieldGroup, FieldLabel, FieldSet,} from "@/components/ui/field";
import {getHomeRouteForRole} from "@/constants/roles";

const LoginComponent = () => {
    const [usernameOrEmail, setUsernameOrEmail] = useState("");
    const [password, setPassword] = useState("");
    const [showPassword, setShowPassword] = useState(false);
    const [isLoading, setIsLoading] = useState(false);
    const [errorMessage, setErrorMessage] = useState("");

    const navigator = useNavigate();

    async function handleLoginForm(e) {
        e.preventDefault();

        const loginObj = {usernameOrEmail, password};

        console.log(loginObj);
        setErrorMessage("");
        setIsLoading(true);

        try {
            const response = await loginAPICall(usernameOrEmail, password);
            console.log(response.data);

            // const token = 'Basic ' + window.btoa(usernameOrEmail + ':' + password);
            const token = "Bearer " + response.data.accessToken;

            const role = response.data.role;

            storeToken(token);
            saveLoggedInUser(usernameOrEmail, role);

            const homeRoute = getHomeRouteForRole(role);
            navigator(homeRoute);

            window.location.reload(false);
        } catch (error) {
            console.error("ERROR1" + error);
            const backendMessage =
                error?.response?.data?.message ||
                error?.response?.data?.error ||
                "Incorrect username or password.";
            setErrorMessage(backendMessage);
        } finally {
            setIsLoading(false);
        }
    }

    return (
        <>
            <div className="fixed top-4 right-4 z-50 print:hidden">
                <ModeToggle/>
            </div>
            <div
                className="bg-muted flex min-h-svh flex-col items-center justify-center gap-6 p-6 md:p-10 overflow-hidden overscroll-none">
                <div className="w-full max-w-md rounded-lg border overflow-hidden shadow-sm">
                    <Card className="rounded-none border-0 shadow-none">
                        <CardHeader>
                            <div className="inline-flex items-baseline justify">
                                <img
                                    src="/wolf-head.png"
                                    alt="WolfCafe Logo"
                                    className="h-8 mr-2 align-baseline"
                                />
                                <span className="text-xl font-medium text-primary">
                                    WolfCafe
                                </span>
                            </div>

                        </CardHeader>
                        <form
                            onSubmit={handleLoginForm}
                            className="flex flex-col gap-6"
                        >
                            <CardContent>
                                <div>
                                    <FieldSet>
                                        <FieldGroup className="gap-2">
                                            <Field>
                                                <FieldLabel htmlFor="username">
                                                    Username
                                                </FieldLabel>
                                                <Input
                                                    id="username"
                                                    type="text"
                                                    placeholder="Enter Username"
                                                    value={usernameOrEmail}
                                                    onChange={(e) =>
                                                        setUsernameOrEmail(
                                                            e.target.value,
                                                        )
                                                    }
                                                />
                                            </Field>
                                            <Field>
                                                <FieldLabel htmlFor="password">
                                                    Password
                                                </FieldLabel>
                                                <InputGroup>
                                                    <InputGroupInput
                                                        id="password"
                                                        type={
                                                            showPassword
                                                                ? "text"
                                                                : "password"
                                                        }
                                                        placeholder="Enter Password"
                                                        value={password}
                                                        onChange={(e) =>
                                                            setPassword(
                                                                e.target.value,
                                                            )
                                                        }
                                                    />
                                                    <InputGroupAddon align="inline-end">
                                                        <InputGroupButton
                                                            size="icon-xs"
                                                            onClick={() =>
                                                                setShowPassword(
                                                                    (prev) =>
                                                                        !prev,
                                                                )
                                                            }
                                                            aria-label={
                                                                showPassword
                                                                    ? "Hide password"
                                                                    : "Show password"
                                                            }
                                                        >
                                                            {showPassword ? (
                                                                <Eye/>
                                                            ) : (
                                                                <EyeClosed/>
                                                            )}
                                                        </InputGroupButton>
                                                    </InputGroupAddon>
                                                </InputGroup>
                                            </Field>
                                        </FieldGroup>
                                    </FieldSet>
                                    {errorMessage && (
                                        <p
                                            className="text-destructive text-sm"
                                            role="alert"
                                        >
                                            {errorMessage}
                                        </p>
                                    )}
                                </div>
                            </CardContent>
                            <CardFooter className="flex-col gap-2">
                                <Button
                                    type="submit"
                                    className="w-full "
                                    disabled={isLoading}
                                >
                                    {isLoading ? "Signing In..." : "Sign In"}
                                </Button>
                                {/*<div className="flex items-center gap-4 ">*/}
                                {/*    <Separator className="flex-1 px-10" />*/}
                                {/*    <span className="text-sm text-muted-foreground">*/}
                                {/*        Or, order without an account*/}
                                {/*    </span>*/}
                                {/*    <Separator className="flex-1 px-10" />*/}
                                {/*</div>*/}
                                {/*<Button*/}
                                {/*    type="button"*/}
                                {/*    variant="secondary"*/}
                                {/*    className="w-full"*/}
                                {/*>*/}
                                {/*    Enter*/}
                                {/*</Button>*/}
                            </CardFooter>
                        </form>
                    </Card>
                    <div className="border-t bg-muted px-6 py-2 text-center">
                        <span className="text-sm text-muted-foreground">
                            Don’t have an account?{" "}
                            <a className="underline" href="/register">
                                Sign Up
                            </a>
                        </span>
                    </div>
                </div>
            </div>
        </>
    );
};

export default LoginComponent;

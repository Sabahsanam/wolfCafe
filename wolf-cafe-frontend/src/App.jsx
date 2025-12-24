import "./index.css";
import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import ListItemsComponent from "./components/ListItemsComponent";
import RegisterComponent from "./components/RegisterComponent";
import LoginComponent from "./components/LoginComponent";
import { isUserLoggedIn } from "./services/AuthService";
import AppSidebarLayout from "./components/AppSidebarLayout";
import RoleProtectedRoute from "./components/RoleProtectedRoute";
import HomeComponent from "./components/HomeComponent";
import PlaceOrderComponent from "./components/PlaceOrderComponent";
import OrdersComponent from "./components/OrdersComponent";
import UsersComponent from "./components/UsersComponent";
import TaxRateComponent from "./components/TaxRateComponent";
import YourOrderComponent from "./components/YourOrderComponent";
import { HOME_ROUTE, ROLES } from "./constants/roles";

const ALL_ROLES = Object.values(ROLES);
const STAFF_AND_ABOVE = [ROLES.STAFF, ROLES.ADMIN];

function App() {
    function ProtectedLayout() {
        const isAuth = isUserLoggedIn();
        if (!isAuth) {
            return <Navigate to="/login" replace />;
        }
        return <AppSidebarLayout />;
    }

    function PublicOnlyRoute({ children }) {
        const isAuth = isUserLoggedIn();
        if (isAuth) {
            return <Navigate to={HOME_ROUTE} replace />;
        }
        return children;
    }

    return (
        <>
            <BrowserRouter>
                <Routes>
                    <Route
                        path="/login"
                        element={
                            <PublicOnlyRoute>
                                <LoginComponent />
                            </PublicOnlyRoute>
                        }
                    />
                    <Route
                        path="/register"
                        element={
                            <PublicOnlyRoute>
                                <RegisterComponent />
                            </PublicOnlyRoute>
                        }
                    />
                    <Route path="/" element={<ProtectedLayout />}>
                        <Route
                            index
                            element={
                                <RoleProtectedRoute allowedRoles={ALL_ROLES}>
                                    <HomeComponent />
                                </RoleProtectedRoute>
                            }
                        />
                        <Route
                            path="/place-order"
                            element={
                                <RoleProtectedRoute allowedRoles={ALL_ROLES}>
                                    <PlaceOrderComponent />
                                </RoleProtectedRoute>
                            }
                        />
                        <Route
                            path="/your-orders"
                            element={
                                <RoleProtectedRoute allowedRoles={ALL_ROLES}>
                                    <YourOrderComponent />
                                </RoleProtectedRoute>
                            }
                        />
                        <Route
                            path="/orders"
                            element={
                                <RoleProtectedRoute allowedRoles={STAFF_AND_ABOVE}>
                                    <OrdersComponent />
                                </RoleProtectedRoute>
                            }
                        />

                        <Route
                            path="/items"
                            element={
                                <RoleProtectedRoute allowedRoles={STAFF_AND_ABOVE}>
                                    <ListItemsComponent />
                                </RoleProtectedRoute>
                            }
                        />
                        <Route
                            path="/users"
                            element={
                                <RoleProtectedRoute allowedRoles={[ROLES.ADMIN]}>
                                    <UsersComponent />
                                </RoleProtectedRoute>
                            }
                        />
                        <Route
                            path="/tax-rates"
                            element={
                                <RoleProtectedRoute allowedRoles={[ROLES.ADMIN]}>
                                    <TaxRateComponent />
                                </RoleProtectedRoute>
                            }
                        />
                        <Route
                            path="*"
                            element={<Navigate to={HOME_ROUTE} replace />}
                        />
                        <Route
                            path="/users"
                            element={<UsersComponent />}
                        />
                    </Route>
                </Routes>
            </BrowserRouter>
        </>
    );
}

export default App;

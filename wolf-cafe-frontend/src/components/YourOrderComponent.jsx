import React, {useEffect, useMemo, useState} from "react";
import {Card, CardContent} from "@/components/ui/card";
import {Button} from "@/components/ui/button";
import {ClipboardCheck, Timer} from "lucide-react";
import {getOrdersByUser, updateOrderStatus} from "../services/OrderService";

const columnTemplate = "180px minmax(220px,2fr) 140px 200px";

const formatPrice = (value) => `$${(Number(value) || 0).toFixed(2)}`;

const YourOrdersComponent = () => {
    const [orders, setOrders] = useState([]);
    const [loading, setLoading] = useState(true);
    const [selectedOrderId, setSelectedOrderId] = useState(null);
    const [actionError, setActionError] = useState("");
    const [actionSuccess, setActionSuccess] = useState("");
    const [isUpdating, setIsUpdating] = useState(false);

    const username = sessionStorage.getItem("authenticatedUser") || "";

    const loadUserOrders = async (options = {}) => {
        const {skipLoading = false} = options;
        if (!skipLoading) {
            setLoading(true);
        }
        try {
            const response = await getOrdersByUser(username);
            const list = Array.isArray(response?.data) ? response.data : [];
            setOrders(list);
            setSelectedOrderId((prev) => {
                if (!prev) return null;
                const stillExists = list.some((order) => order.id === prev);
                return stillExists ? prev : null;
            });
        } catch (error) {
            console.error("Error fetching user orders:", error);
            setOrders([]);
        } finally {
            if (!skipLoading) {
                setLoading(false);
            }
        }
    };

    useEffect(() => {
        if (!username) {
            setLoading(false);
            return;
        }
        loadUserOrders();
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [username]);

    useEffect(() => {
        if (!username) return undefined;

        const intervalId = setInterval(() => {
            loadUserOrders({skipLoading: true});
        }, 4000); // lightweight polling to auto-refresh status

        return () => clearInterval(intervalId);
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [username]);

    const selectedOrder = useMemo(
        () => orders.find((order) => order.id === selectedOrderId) ?? null,
        [orders, selectedOrderId],
    );

    const selectedLines = selectedOrder?.orderLines ?? [];
    const selectedSubtotal = selectedLines.reduce(
        (sum, line) =>
            sum +
            (Number(line?.price) || 0) * (Number(line?.amount) || 0),
        0,
    );

    const tipAmount = Number(selectedOrder?.tip) || 0;
    const taxRate = Number(selectedOrder?.taxrate) || 0;
    const taxAmount = selectedSubtotal * (taxRate / 100);
    const totalAmount =
        selectedOrder?.totalPrice ??
        (selectedSubtotal + tipAmount + taxAmount);

    const formatOrderNumber = (order) =>
        order?.id ? `O-${String(order.id).padStart(6, "0")}` : "O-—";

    const getStatusPill = (status, size = "sm") => {
        const isPending = status === "PENDING";
        const isFulfilled = status === "FULFILLED";
        const isPickedUp = status === "PICKED_UP";
        const Icon = isPending ? Timer : ClipboardCheck;

        const baseClasses =
            "inline-flex items-center gap-1.5 rounded-full border px-3 py-1 font-medium";
        let colors = "text-muted-foreground border-border bg-muted/40";

        if (isPending) {
            colors = "text-amber-700 border-amber-200 bg-amber-50";
        } else if (isFulfilled) {
            colors = "text-blue-600 border-blue-200 bg-blue-50";
        } else if (isPickedUp) {
            colors = "text-emerald-700 border-emerald-200 bg-emerald-50";
        }

        const textSize = size === "base" ? "text-base" : "text-sm";

        const text = isPending
            ? "Pending"
            : isFulfilled
                ? "Ready for Pickup"
                : isPickedUp
                    ? "Picked Up"
                    : status || "Unknown";

        return (
            <span className={`${baseClasses} ${colors} ${textSize}`}>
                <Icon className="size-4"/>
                {text}
            </span>
        );
    };

    const handleSelectRow = (orderId) => {
        setSelectedOrderId(orderId);
        setActionError("");
        setActionSuccess("");
    };

    const pickupLabel = () => {
        if (!selectedOrder) return "Pick Up";

        if (selectedOrder.status === "PICKED_UP") {
            return "Picked Up";
        }

        if (selectedOrder.status === "FULFILLED") {
            return isUpdating ? "Picking Up..." : "Pick Up";
        }

        // PENDING or any other state:
        return "Pick Up";  // disabled will indicate it's not available
    };

    const handlePickup = async () => {
        if (!selectedOrder || selectedOrder.status !== "FULFILLED") return;
        if (isUpdating) return;

        setIsUpdating(true);
        setActionError("");
        setActionSuccess("");

        try {
            await updateOrderStatus(selectedOrder.id, "PICKED_UP");
            setOrders((prev) =>
                prev.map((order) =>
                    order.id === selectedOrder.id
                        ? {...order, status: "PICKED_UP"}
                        : order,
                ),
            );
            setActionSuccess("Order marked as picked up.");
        } catch (error) {
            console.error("Failed to pick up order", error);
            const backendMessage =
                error?.response?.data?.message ||
                error?.response?.data?.error ||
                (typeof error?.response?.data === "string"
                    ? error.response.data
                    : null);
            setActionError(backendMessage || "Unable to pick up order.");
        } finally {
            setIsUpdating(false);
        }
    };

    const renderItemsSummary = (order) => {
        if (!order?.orderLines?.length) return "—";
        return order.orderLines
            .map((line) => {
                const name = line?.itemName ?? "Item";
                const amount = line?.amount ?? 0;
                return `${name} x${amount}`;
            })
            .join(", ");
    };

    const ordersContent = () => {
        if (!username) {
            return (
                <div className="border border-dashed border-border rounded-xl p-6 text-center text-muted-foreground">
                    You must be logged in to view your orders.
                </div>
            );
        }

        if (loading) {
            return (
                <div className="border border-dashed border-border rounded-xl p-6 text-center text-muted-foreground">
                    Loading your orders...
                </div>
            );
        }

        if (!orders.length) {
            return (
                <div className="border border-dashed border-border rounded-xl p-6 text-center text-muted-foreground">
                    You don’t have any orders yet.
                </div>
            );
        }

        return (
            <Card className="overflow-hidden border-2 shadow-none p-0">
                <div className="overflow-x-auto">
                    <div className="inline-block min-w-full">
                        <div
                            className="grid bg-muted border-b-2 border-border"
                            style={{gridTemplateColumns: columnTemplate}}
                        >
                            {["Order ID", "Items", "Total", "Status"].map((label) => (
                                <div
                                    key={label}
                                    className="border-r border-border h-10 font-medium flex items-center px-5 text-foreground"
                                >
                                    {label}
                                </div>
                            ))}
                        </div>

                        <div className="max-h-[540px] overflow-y-auto">
                            {orders.map((order) => {
                                const isSelected = order.id === selectedOrderId;
                                return (
                                    <div
                                        key={order.id}
                                        role="button"
                                        tabIndex={0}
                                        onClick={() => handleSelectRow(order.id)}
                                        onKeyDown={(e) => {
                                            if (e.key === "Enter" || e.key === " ") {
                                                e.preventDefault();
                                                handleSelectRow(order.id);
                                            }
                                        }}
                                        className={`grid border-b border-border text-base transition-colors ${
                                            isSelected
                                                ? "bg-muted/60"
                                                : "hover:bg-muted/30"
                                        }`}
                                        style={{gridTemplateColumns: columnTemplate}}
                                    >
                                        <span
                                            className="border-r border-border px-5 py-3 tabular-nums flex items-center text-foreground">
                                            {formatOrderNumber(order)}
                                        </span>
                                        <span
                                            className="border-r border-border px-5 py-3 flex items-center text-foreground">
                                            {renderItemsSummary(order)}
                                        </span>
                                        <span
                                            className="border-r border-border px-5 py-3 font-semibold tabular-nums flex items-center text-foreground">
                                            {formatPrice(order?.totalPrice)}
                                        </span>
                                        <span className="px-5 py-3 flex items-center">
                                            {getStatusPill(order?.status)}
                                        </span>
                                    </div>
                                );
                            })}
                        </div>
                    </div>
                </div>
            </Card>
        );
    };

    return (
        <div className="min-h-full bg-background p-8 overscroll-contain flex flex-col gap-6">
            <div className="space-y-2">
                <h2 className="text-xl font-semibold">Your Orders</h2>

            </div>

            {ordersContent()}

            {selectedOrder && (
                <div className="space-y-3 max-w-3xl">
                    <h3 className="text-xl font-semibold">Order Details</h3>
                    <Card>
                        <CardContent className="flex flex-col gap-5 pt-6">
                            <div className="flex flex-wrap items-start justify-between gap-3">
                                <div className="space-y-1 text-sm text-muted-foreground">
                                    <p className="text-lg font-medium text-foreground">
                                        {username || "You"}
                                    </p>
                                    <p className="text-sm text-foreground">
                                        {formatOrderNumber(selectedOrder)}
                                    </p>
                                </div>
                                <div>{getStatusPill(selectedOrder?.status, "base")}</div>
                            </div>

                            <div className="grid gap-6 lg:grid-cols-[minmax(0,1.4fr)_minmax(200px,0.6fr)]">
                                <div className="space-y-5">
                                    <div className="space-y-3">
                                        {selectedLines.length === 0 ? (
                                            <p className="text-sm text-muted-foreground">
                                                No items on this order.
                                            </p>
                                        ) : (
                                            selectedLines.map((line, idx) => {
                                                const lineTotal =
                                                    (Number(line?.price) || 0) *
                                                    (Number(line?.amount) || 0);
                                                return (
                                                    <div
                                                        key={`${line?.itemId ?? idx}-${line?.itemName ?? "item"}`}
                                                        className="grid grid-cols-[3ch_minmax(0,1fr)_auto] items-center gap-3 text-sm tabular-nums"
                                                    >
                                                        <span className="font-medium">
                                                            {line?.amount ?? 0}
                                                        </span>
                                                        <span className="truncate">
                                                            {line?.itemName ?? "Item"}
                                                        </span>
                                                        <span className="text-right font-medium">
                                                            {formatPrice(lineTotal)}
                                                        </span>
                                                    </div>
                                                );
                                            })
                                        )}
                                    </div>

                                    <div className="space-y-1 border-t pt-3 text-sm">
                                        <div className="flex items-center justify-between">
                                            <span>Subtotal</span>
                                            <span className="font-medium">
                                                {formatPrice(selectedSubtotal)}
                                            </span>
                                        </div>
                                        <div className="flex items-center justify-between">
                                            <span>Tax ({taxRate.toFixed(1)}%)</span>
                                            <span className="font-medium">
                                                {formatPrice(taxAmount)}
                                            </span>
                                        </div>
                                        <div className="flex items-center justify-between">
                                            <span>Tip</span>
                                            <span className="font-medium">
                                                {formatPrice(tipAmount)}
                                            </span>
                                        </div>
                                        <div className="flex items-center justify-between text-base font-semibold pt-1">
                                            <span>Total</span>
                                            <span>{formatPrice(totalAmount)}</span>
                                        </div>
                                    </div>
                                </div>

                                <div className="rounded-xl bg-muted/40 p-4 flex flex-col justify-between">
                                    <div className="space-y-2">
                                        {actionError && (
                                            <p className="text-sm text-destructive">
                                                {actionError}
                                            </p>
                                        )}
                                        {actionSuccess && (
                                            <p className="text-sm text-emerald-600">
                                                {actionSuccess}
                                            </p>
                                        )}
                                    </div>
                                    <Button
                                        className="mt-auto h-12 text-lg"
                                        disabled={
                                            selectedOrder.status !== "FULFILLED" ||
                                            isUpdating
                                        }
                                        onClick={handlePickup}
                                    >
                                        {pickupLabel()}
                                    </Button>
                                </div>
                            </div>
                        </CardContent>
                    </Card>
                </div>
            )}
        </div>
    );
};

export default YourOrdersComponent;

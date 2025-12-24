import React, {useEffect, useMemo, useState} from 'react';
import {getAllItems} from '../services/ItemService';
import {placeOrder} from '../services/OrderService';
import {useNavigate} from 'react-router-dom';
import {getTax} from '../services/TaxService';
import {Card, CardContent, CardFooter} from "@/components/ui/card.tsx";
import {Button} from "@/components/ui/button.tsx";
import {Input} from "@/components/ui/input.tsx";
import {Toggle} from "@/components/ui/toggle.tsx";
import {Check, X} from "lucide-react";

const PlaceOrderComponent = () => {
    const [items, setItems] = useState([]);
    const [quantities, setQuantities] = useState({});
    const [loading, setLoading] = useState(true);
    const [submitting, setSubmitting] = useState(false);
    const [fetchError, setFetchError] = useState("");
    const [taxRate, setTaxRate] = useState(0);
    const [tip, setTip] = useState(0);
    const [tipSelection, setTipSelection] = useState(null);
    const [customTipInput, setCustomTipInput] = useState("");
    const navigate = useNavigate();

    const parseCurrencyValue = (value) => {
        if (!value) return NaN;
        const numericValue = value.replace(/[^0-9.]/g, "");
        if (numericValue === "" || numericValue === ".") return NaN;
        const parsed = parseFloat(numericValue);
        return Number.isFinite(parsed) ? parsed : NaN;
    };

    const sanitizeCustomTipInput = (value) => {
        if (!value) return "";
        let sanitized = "";
        let hasDecimal = false;
        for (const char of value) {
            if (/\d/.test(char)) {
                sanitized += char;
                continue;
            }
            if (char === "." && !hasDecimal) {
                hasDecimal = true;
                sanitized += ".";
            }
        }

        if (sanitized === "" || sanitized === ".") {
            return hasDecimal ? "0." : "";
        }

        const endsWithDecimal = sanitized.endsWith(".");
        const [integerRaw = "0", decimalRaw = ""] = sanitized.split(".");
        const integerPart = integerRaw.replace(/^0+(?=\d)/, "") || "0";
        const decimals = decimalRaw.slice(0, 2);

        if (endsWithDecimal && decimals === "") {
            return `${integerPart}.`;
        }

        return decimals === "" ? integerPart : `${integerPart}.${decimals}`;
    };

    useEffect(() => {
        const fetchItems = async () => {
            setLoading(true);
            try {
                const res = await getAllItems();
                setItems(res.data);

                const taxRate = await getTax();
                setTaxRate(taxRate.data.rate);
                setFetchError("");
            } catch (error) {
                console.error("Error loading items: ", error);
                setFetchError("Unable to load menu items.")
            } finally {
                setLoading(false);
            }
        };
        fetchItems();


    }, []);

    const updateAmount = (itemId, value) => {
        setQuantities({
            ...quantities,
            [itemId]: Math.max(0, Number(value))
        });
    };

    const selectedLines = useMemo(
        () =>
            items.filter((item) => {
                const id = item?.id ?? null;
                if (id == null) return false;
                return (quantities[id] ?? 0) > 0;
            }),
        [items, quantities],
    );


    const subTotal = items.reduce((total, item) => {
        const quantity = quantities[item.id] || 0;
        return total + quantity * item.price;
    }, 0);

    const taxAmount = subTotal * (taxRate / 100);
    const total = subTotal + taxAmount + tip;

    const menuColumnTemplate = "minmax(260px,1.5fr) 120px 180px 180px";
    const tipPresets = [
        {label: "15%", value: 0.15},
        {label: "20%", value: 0.2},
        {label: "25%", value: 0.25},
    ]

    useEffect(() => {
        if (!tipSelection) {
            setTip(0);
            return;
        }

        if (tipSelection.type === "percent") {
            const calculated = subTotal * tipSelection.value;
            setTip(Number.isFinite(calculated) ? Number(calculated.toFixed(2)) : 0);
            return;
        }

        const parsedCustomTip = parseCurrencyValue(customTipInput);
        setTip(
            Number.isFinite(parsedCustomTip)
                ? Number(parsedCustomTip.toFixed(2))
                : 0,
        );
    }, [tipSelection, subTotal, customTipInput]);

    const isCustomTipSelected = tipSelection?.type === "custom";
    const hasValidCustomTip =
        !isCustomTipSelected ||
        Number.isFinite(parseCurrencyValue(customTipInput));
    const isTipChosen = Boolean(tipSelection) && hasValidCustomTip;

    const handleTipPresetClick = (preset, pressed) => {
        if (!pressed) {
            return;
        }

        if (preset.custom) {
            setTipSelection({type: "custom"});
            return;
        }
        setTipSelection({type: "percent", value: preset.value});
    };

    const handleCustomTipChange = (value) => {
        const sanitized = sanitizeCustomTipInput(value);
        setCustomTipInput(sanitized);
    };

    const handleCustomTipBlur = () => {
        const parsed = parseCurrencyValue(customTipInput);
        if (Number.isFinite(parsed)) {
            setCustomTipInput(parsed.toFixed(2));
        } else if (customTipInput === "." || customTipInput === "0.") {
            setCustomTipInput("0.00");
        } else if (customTipInput !== "") {
            setCustomTipInput("");
        }
    };

    const submitOrder = async () => {
        const orderLines = Object.entries(quantities)
            .filter(([_, qty]) => qty > 0)
            .map(([itemId, qty]) => ({
                itemId: Number(itemId),
                amount: qty
            }));

        if (orderLines.length === 0) {
            alert("Please select at least one item to place an order.");
            return;
        }

        if (!isTipChosen) {
            alert("Please select a tip option before placing the order.");
            return;
        }

        try {
            await placeOrder({orderLines, tip, taxrate: taxRate});
            navigate("/your-orders");
        } catch (error) {
            console.error("Error placing order: ", error);
            alert("Error placing order.");
        }
    };


    const renderMenuGrid = () => {
        if (loading || fetchError || items.length === 0) {
            let message = "No items found.";
            let messageClass = "text-muted-foreground";

            if (loading) {
                message = "Loading menu...";
            } else if (fetchError) {
                message = fetchError;
                messageClass = "text-destructive";
            }

            return (
                <div
                    className="grid"
                    style={{gridTemplateColumns: menuColumnTemplate}}
                >
                    <div
                        className={`col-span-4 border-b border-border px-6 py-10 text-center text-sm ${messageClass}`}
                    >
                        {message}
                    </div>

                </div>
            );
        }
        return (
            <div
                className="grid"
                style={{gridTemplateColumns: menuColumnTemplate}}
            >
                {items.map((item) => {
                    const id = item?.id ?? null;
                    const available = item?.amount ?? 0;
                    const isAvailable = available > 0;
                    const qty = quantities[id] ?? 0;
                    const disableIncrement = !isAvailable || qty >= available;
                    const disableDecrement = qty <= 0;

                    return (
                        <React.Fragment key={id ?? item?.name}>
                            <div className="border-b border-r border-border px-5 py-4 flex items-center">
                                <div className="flex flex-col justify-center">
                                    <p className="font-medium">
                                        {item?.name ?? "—"}
                                    </p>
                                    {item?.description && (
                                        <p className="text-xs text-muted-foreground">
                                            {item.description}
                                        </p>
                                    )}
                                </div>
                            </div>
                            <div className="border-b border-r border-border px-5 py-4  flex items-center">
                                ${item.price.toFixed(2)}
                            </div>
                            <div className="border-b border-r border-border px-5 py-4 flex items-center">
                              <span
                                  className={`flex items-center gap-2 ${
                                      isAvailable ? "text-foreground" : "text-destructive"
                                  }`}
                              >
                                {isAvailable ? (
                                    <Check className="h-6 w-6 shrink-0"/>
                                ) : (
                                    <X className="h-6 w-6 shrink-0"/>
                                )}

                                  {isAvailable ? `In Stock (${available})` : "Out of Stock"}
                              </span>
                            </div>
                            <div className="border-b border-border px-5 py-3 items-center">
                                <div className="flex items-center gap-2">
                                    <Button
                                        type="button"
                                        variant="ghost"
                                        size="icon-sm"
                                        className="text-lg"
                                        onClick={() =>
                                            updateAmount(id, qty - 1)
                                        }
                                        disabled={disableDecrement}
                                        aria-label={`Decrease ${item?.name}`}
                                    >
                                        -
                                    </Button>
                                    {/*<span className="w-8 text-center font-semibold">*/}
                                    {/*        {qty}*/}
                                    {/*    </span>*/}
                                    <Input
                                        className={"w-12 text-center"}

                                        min="0"
                                        value={qty}
                                        onChange={(e) =>
                                            updateAmount(item.id, e.target.value)
                                        }
                                    >

                                    </Input>

                                    <Button
                                        type="button"
                                        variant="ghost"
                                        size="icon-sm"
                                        className="text-lg"
                                        onClick={() =>
                                            updateAmount(id, qty + 1)
                                        }
                                        disabled={disableIncrement}
                                        aria-label={`Decrease ${item?.name}`}
                                    >
                                        +
                                    </Button>
                                </div>
                            </div>


                        </React.Fragment>
                    );
                })}
            </div>
        );
    };


    return (
        <div className="min-h-full bg-background p-8 overscroll-contain">
            <div className="space-y-6">
                <div className="grid gap-6 lg:grid-cols-[minmax(0,2fr)_minmax(0,1fr)]">
                    <div className="space-y-3">
                        <h2 className="text-xl font-semibold">Items</h2>
                        <Card className="overflow-hidden border-2 shadow-none p-0">
                            <div className="overflow-x-auto">
                                <div className="inline-block min-w-full">
                                    <div
                                        className="grid bg-muted border-b-2 border-border font-medium text-foreground"
                                        style={{
                                            gridTemplateColumns: menuColumnTemplate,
                                        }}
                                    >
                                        {["Item", "Price", "Available", "Quantity"].map(
                                            (label) => (
                                                <div
                                                    key={label}
                                                    className="border-r border-border h-12 flex items-center px-5"
                                                >
                                                    {label}
                                                </div>
                                            ),
                                        )}
                                    </div>

                                    <div className="max-h-[520px] overflow-y-auto">
                                        {renderMenuGrid()}
                                    </div>
                                </div>
                            </div>
                        </Card>
                    </div>

                    <div className="space-y-3">
                        <h2 className="text-xl font-semibold">Your Order</h2>
                        <Card className="h-fit">
                            <CardContent className="space-y-6">
                                <div>
                                    <p className="text-xs font-semibold uppercase tracking-wide text-muted-foreground">
                                        Items
                                    </p>
                                    <div className="mt-3 space-y-3">
                                        {selectedLines.length === 0 ? (
                                            <p className="text-sm text-muted-foreground">
                                                No items selected yet.
                                            </p>
                                        ) : (
                                            selectedLines.map((item) => {
                                                const id = item?.id ?? null;
                                                const qty = quantities[id] ?? 0;
                                                const lineTotal = qty * (Number(item?.price) || 0);
                                                return (
                                                    <div
                                                        key={
                                                            id ?? `${item?.name}-summary`
                                                        }
                                                        className="grid grid-cols-[3ch_minmax(0,1fr)_auto] items-center gap-3 text-sm tabular-nums"
                                                    >
                                                        <span className="font-medium">
                                                            {qty}
                                                        </span>
                                                        <span className="font-medium truncate">
                                                            {item?.name}
                                                        </span>
                                                        <span className="text-right font-medium">
                                                            ${(lineTotal).toFixed(2)}
                                                        </span>
                                                    </div>
                                                );
                                            })
                                        )}
                                    </div>
                                </div>

                                <div className="space-y-2">
                                    <div className="flex items-center justify-between text-sm font-semibold">
                                        <span>Subtotal</span>
                                        <span>${(subTotal).toFixed(2)}</span>
                                    </div>
                                </div>

                                <div className="space-y-3">
                                    <p className="text-xs font-semibold uppercase tracking-wide text-muted-foreground">
                                        Tip
                                    </p>
                                </div>
                                <div className="grid grid-cols-2 gap-2">
                                    {[...tipPresets, {label: "Custom", custom: true}].map(
                                        (preset) => {
                                            const isSelected = preset.custom
                                                ? isCustomTipSelected
                                                : tipSelection?.type === "percent" &&
                                                tipSelection.value === preset.value;
                                            return (
                                                <Toggle
                                                    key={preset.label}
                                                    type="button"
                                                    variant={isSelected ? "default" : "outline"}
                                                    pressed={isSelected}
                                                    className={`text-sm ${
                                                        preset.custom
                                                            ? "justify-center"
                                                            : "justify-between"
                                                    }`}
                                                    onPressedChange={(pressed) =>
                                                        handleTipPresetClick(preset, pressed)
                                                    }
                                                >
                                                    <span>{preset.label}</span>
                                                    {!preset.custom && (
                                                        <span className="tabular-nums text-muted-foreground">
                                                           (${(subTotal * preset.value).toFixed(2)})
                                                        </span>
                                                    )}
                                                </Toggle>
                                            );
                                        },
                                    )}
                                </div>
                                {isCustomTipSelected && (
                                    <div className="space-y-1">
                                        <p className="text-xs font-semibold uppercase tracking-wide text-muted-foreground">
                                            Custom Tip Amount
                                        </p>
                                        <div className="relative">
                                            <span
                                                className="pointer-events-none absolute inset-y-0 left-3 flex items-center text-sm text-muted-foreground">
                                                $
                                            </span>
                                            <Input
                                                type="text"
                                                inputMode="decimal"
                                                placeholder="0.00"
                                                value={customTipInput}
                                                className="pl-7"
                                                onChange={(e) =>
                                                    handleCustomTipChange(e.target.value)
                                                }
                                                onBlur={handleCustomTipBlur}
                                            />
                                        </div>
                                    </div>
                                )}
                                <div className="space-y-2">
                                    <div className="flex items-center justify-between text-sm font-semibold">
                                        <span>Tip</span>
                                        <span>${tip.toFixed(2)}</span>
                                    </div>
                                </div>
                                <div className="flex items-center justify-between text-sm font-semibold">
                                        <span>
                                            Tax (
                                            {(taxRate).toFixed(
                                                2,
                                            )}
                                            %)
                                        </span>
                                    <span>
                                            ${(taxAmount).toFixed(2)}
                                    </span>
                                </div>
                                <div className="flex items-center justify-between text-sm font-semibold">
                                        <span>
                                            Total
                                        </span>
                                    <span>
                                            ${total.toFixed(2)}
                                    </span>
                                </div>

                            </CardContent>
                            <CardFooter>
                                <Button
                                    className="w-full text-base h-11"
                                    size="lg"
                                    disabled={
                                        selectedLines.length === 0 ||
                                        submitting ||
                                        !isTipChosen
                                    }
                                    onClick={submitOrder}
                                >
                                    {submitting
                                        ? "Placing order..."
                                        : "Place order"}
                                </Button>
                            </CardFooter>
                        </Card>
                    </div>
                </div>
            </div>


        </div>
    );
};

export default PlaceOrderComponent;

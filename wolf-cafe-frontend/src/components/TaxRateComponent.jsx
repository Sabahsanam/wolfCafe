import React, { useEffect, useState } from "react";
import { getTax, setTax } from "../services/TaxService";
import { Card, CardContent, CardFooter } from "@/components/ui/card.tsx";
import { Input } from "@/components/ui/input.tsx";
import { Button } from "@/components/ui/button.tsx";
import { Label } from "@/components/ui/label.tsx";

const TaxRateComponent = () => {
    const [taxRate, setTaxRate] = useState("");
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");
    const [success, setSuccess] = useState(false);

    const token = localStorage.getItem("token");

    useEffect(() => {
        const fetchTaxRate = async () => {
            try {
                if (!token) {
                    setError("Not authenticated.");
                    return;
                }

                const response = await getTax(token);

                setTaxRate(String(response.data.rate));
            } catch (err) {
                console.error("Error fetching tax:", err);
                setError("Failed to load tax rate.");
            } finally {
                setLoading(false);
            }
        };

        fetchTaxRate();
    }, [token]);

    const handleSubmit = async () => {
        try {
            setError("");
            setSuccess(false);

            const parsed = parseFloat(taxRate);
            if (isNaN(parsed)) {
                setError("Please enter a valid tax rate.");
                return;
            }

            await setTax(token, parsed);
            setSuccess(true);

            setTimeout(() => setSuccess(false), 2000);
        } catch (err) {
            console.error("Error updating tax:", err);

            // Try to pull a useful message from the backend response
            const backendMessage =
                err?.response?.data?.message ||    // e.g. { "message": "Tax rate cannot be negative." }
                err?.response?.data?.error ||      // sometimes APIs use "error"
                (typeof err?.response?.data === "string"
                    ? err.response.data
                    : null);

            setError(backendMessage || "Failed to update tax.");
        }
    };

    if (loading) {
        return <div>Loading tax rate...</div>;
    }

    return (
        <div className="min-h-full bg-background p-8 overscroll-contain flex flex-col gap-6">
            <div className="space-y-2">
                <h2 className="text-xl font-semibold">Tax Rate</h2>

                <Card className="max-w-60">
                    <CardContent>
                        <div className="space-y-2">
                            <Label
                                htmlFor="tax-rate"
                                className="text-sm font-medium leading-none"
                            >
                                Tax rate
                            </Label>

                            <div className="flex items-center gap-2">
                                <div className="relative">
                                    <Input
                                        id="tax-rate"
                                        type="number"
                                        inputMode="decimal"
                                        min={0}
                                        max={100}
                                        step="0.01"
                                        value={taxRate}
                                        onChange={(e) => setTaxRate(e.target.value)}
                                        className="w-28 pr-8 text-right"
                                    />
                                    <span
                                        className="pointer-events-none absolute inset-y-0 right-2 flex items-center text-sm text-muted-foreground">
                                        %
                                    </span>
                                </div>
                            </div>


                        </div>
                    </CardContent>

                    <CardFooter className="flex flex-col gap-2">
                        <Button className="w-full" onClick={handleSubmit}>
                            Update
                        </Button>

                        {error && (
                            <p className="text-sm text-destructive">
                                {error}
                            </p>
                        )}
                        {success && (
                            <p className="text-sm text-emerald-500">
                                Tax updated successfully.
                            </p>
                        )}
                    </CardFooter>
                </Card>


            </div>
        </div>
    );
};

export default TaxRateComponent;

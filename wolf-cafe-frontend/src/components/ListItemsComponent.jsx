import React, {
    useCallback,
    useEffect,
    useMemo,
    useRef,
    useState,
} from "react";
import { Card } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Undo2, AlertCircle } from "lucide-react";
import { Alert, AlertTitle, AlertDescription } from "@/components/ui/alert";

// Services (your file names/paths may differ)
import {
    getAllItems,
    updateItem,
    saveItem, // <- create
    // deleteItemById // optional
} from "../services/ItemService";

export default function ListItemsComponent() {
    const headers = useMemo(
        () => ["Name", "Description", "Price", "Amount"],
        [],
    );

    // Robust ID extraction in case backend uses a different key
    const getItemId = (item) => item?.id ?? item?.itemId ?? item?._id ?? null;

    // ---- cell/row helpers ----
    const makeCell = (rowIndex, colIndex, value) => ({
        id: `${rowIndex}-${colIndex}`,
        value: value ?? "",
        isDirty: false,
        isInvalid: false,
    });

    const asRow = (rowIndex, item) => [
        makeCell(rowIndex, 0, item?.name ?? ""),
        makeCell(rowIndex, 1, item?.description ?? ""),
        makeCell(rowIndex, 2, item?.price?.toString?.() ?? ""),
        makeCell(rowIndex, 3, item?.amount?.toString?.() ?? ""),
    ];

    const placeholderText = [
        "Add Item...",
        "Enter description...",
        "0.00",
        "0",
    ];
    const createPlaceholderRow = (rowIndex) =>
        placeholderText.map((v, i) => makeCell(rowIndex, i, v));

    // ---- state ----
    const [data, setData] = useState([]); // rows of CellData[]
    const [originalData, setOriginalData] = useState([]); // cloned from loaded data (no placeholder)
    const [backingIds, setBackingIds] = useState([]); // rowIndex -> backend id | null
    const [editingCell, setEditingCell] = useState(null);
    const [editValue, setEditValue] = useState("");
    const [isSaving, setIsSaving] = useState(false);
    const [saveError, setSaveError] = useState(null);
    const [focusedCell, setFocusedCell] = useState(null);
    const cellRefs = useRef([]);

    const focusDomCell = useCallback((row, col) => {
        const el = cellRefs.current?.[row]?.[col];
        if (el && typeof el.focus === "function") {
            el.focus();
            if (typeof el.scrollIntoView === "function") {
                el.scrollIntoView({ block: "nearest", inline: "nearest" });
            }
        }
    }, []);

    const focusCell = useCallback(
        (row, col) => {
            if (data.length === 0) return;
            const maxRow = data.length - 1;
            const maxCol = headers.length - 1;
            const nextRow = Math.max(0, Math.min(row, maxRow));
            const nextCol = Math.max(0, Math.min(col, maxCol));

            focusDomCell(nextRow, nextCol);
            setFocusedCell((prev) => {
                if (prev && prev.row === nextRow && prev.col === nextCol)
                    return prev;
                return { row: nextRow, col: nextCol };
            });
        },
        [data.length, headers.length, focusDomCell],
    );

    const setCellRef = useCallback(
        (el, row, col) => {
            if (!cellRefs.current[row]) {
                cellRefs.current[row] = Array(headers.length).fill(null);
            }
            cellRefs.current[row][col] = el;
        },
        [headers.length],
    );

    // ---- load from backend ----
    useEffect(() => {
        let mounted = true;
        (async () => {
            try {
                const res = await getAllItems();
                const items = Array.isArray(res?.data) ? res.data : [];
                if (!mounted) return;

                const rows = items.map((item, idx) => asRow(idx, item));
                const ids = items.map((item) => getItemId(item));

                const working = [...rows, createPlaceholderRow(rows.length)];
                setData(working);
                setOriginalData(rows.map((r) => r.map((c) => ({ ...c }))));
                setBackingIds(ids);
            } catch (e) {
                console.error("Failed to load items", e);
                setData([createPlaceholderRow(0)]);
                setOriginalData([]);
                setBackingIds([]);
            }
        })();
        return () => {
            mounted = false;
        };
    }, []);

    useEffect(() => {
        if (data.length === 0) {
            cellRefs.current = [];
            return;
        }

        const nextRefs = Array.from({ length: data.length }, (_, rowIndex) => {
            const existingRow = cellRefs.current[rowIndex] ?? [];
            const trimmed = existingRow.slice(0, headers.length);
            while (trimmed.length < headers.length) {
                trimmed.push(null);
            }
            return trimmed;
        });

        cellRefs.current = nextRefs;
    }, [data.length, headers.length]);

    useEffect(() => {
        if (data.length > 0 && focusedCell) {
            focusCell(focusedCell.row, focusedCell.col);
        }
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [data.length, focusCell]);

    useEffect(() => {
        if (!editingCell) return;
        const [rowSegment, colSegment] = editingCell.split("-");
        const row = Number(rowSegment);
        const col = Number(colSegment);
        if (Number.isNaN(row) || Number.isNaN(col)) return;

        const target = cellRefs.current?.[row]?.[col];
        if (!target) return;

        const placeCaret = () => {
            const el = cellRefs.current?.[row]?.[col];
            if (!el) return;
            if (typeof el.setSelectionRange === "function") {
                const length = el.value?.length ?? 0;
                el.setSelectionRange(length, length);
            }
            if (
                typeof el.scrollTop === "number" &&
                typeof el.scrollHeight === "number"
            ) {
                el.scrollTop = el.scrollHeight;
            }
        };

        if (
            typeof window !== "undefined" &&
            typeof window.requestAnimationFrame === "function"
        ) {
            window.requestAnimationFrame(placeCaret);
        } else {
            setTimeout(placeCaret, 0);
        }
    }, [editingCell]);

    // ---- utils/validation ----
    const isPlaceholderRow = (rowIndex) => {
        if (rowIndex !== data.length - 1) return false;
        const row = data[rowIndex];
        return row.every((cell, i) => cell.value === placeholderText[i]);
    };

    const formatCellValueForDisplay = (value, colIndex) => {
        if (colIndex === 2 && value !== "" && !isNaN(Number(value))) {
            return `$${Number(value).toFixed(2)}`;
        }
        return value;
    };

    const parsePrice = (val) =>
        typeof val === "string" ? val.replace(/^\$/, "") : val;

    const isValidCell = (value, colIndex) => {
        if (colIndex === 1) return true; // description optional
        const trimmed = String(value ?? "").trim();
        if (trimmed === "") return false;
        if (colIndex === 0) return true; // name required, any text
        if (colIndex === 2) return !isNaN(Number(parsePrice(trimmed))); // price number
        if (colIndex === 3) return /^\d+$/.test(trimmed); // Amount integer
        return false;
    };

    const isRowModified = (rowIndex) => {
        if (!originalData[rowIndex]) return false;
        return data[rowIndex].some(
            (cell, colIndex) =>
                cell.value !== originalData[rowIndex][colIndex].value,
        );
    };

    // ---- editing ----
    const handleCellClick = (rowIndex, colIndex) => {
        setFocusedCell({ row: rowIndex, col: colIndex });
        const cellId = `${rowIndex}-${colIndex}`;
        if (isPlaceholderRow(rowIndex)) {
            const next = [...data];
            next[rowIndex] = [
                makeCell(rowIndex, 0, ""),
                makeCell(rowIndex, 1, ""),
                makeCell(rowIndex, 2, ""),
                makeCell(rowIndex, 3, ""),
            ];
            next.push(createPlaceholderRow(next.length));
            setData(next);
            setBackingIds((prev) => [...prev, null]); // mark as new
            setEditValue("");
            setEditingCell(cellId);
            return;
        }
        const current = data[rowIndex][colIndex].value;
        setEditValue(colIndex === 2 ? parsePrice(current) : current);
        setEditingCell(cellId);
    };

    const handleCellChange = (val) => setEditValue(val);

    const handleCellBlur = (rowIndex, colIndex, options = {}) => {
        const { suppressRefocus = false } = options;
        const next = [...data];
        const originalValue = originalData[rowIndex]?.[colIndex]?.value ?? "";
        const candidate = colIndex === 2 ? parsePrice(editValue) : editValue;
        const valid = isValidCell(candidate, colIndex);

        next[rowIndex][colIndex].value = candidate;
        next[rowIndex][colIndex].isDirty =
            !!originalData[rowIndex] && candidate !== originalValue;
        next[rowIndex][colIndex].isInvalid = !valid;

        setData(next);
        setEditingCell(null);
        if (!suppressRefocus) {
            setTimeout(() => focusCell(rowIndex, colIndex), 0);
        }
    };

    const handleEditorKeyDown = (e, rowIndex, colIndex) => {
        if (e.key === "Tab") {
            return;
        }

        if (e.key === "Enter") {
            handleCellBlur(rowIndex, colIndex);
        } else if (e.key === "Escape") {
            setEditingCell(null);
            setTimeout(() => focusCell(rowIndex, colIndex), 0);
        }
    };

    const handleGridKeyDown = (e) => {
        const navWhileEditing =
            (e.ctrlKey || e.metaKey) && editingCell !== null;
        const notEditing = editingCell === null;
        const maxRow = data.length > 0 ? data.length - 1 : 0;
        const maxCol = headers.length - 1;

        if (!focusedCell) {
            if (
                [
                    "ArrowUp",
                    "ArrowDown",
                    "ArrowLeft",
                    "ArrowRight",
                    "Home",
                    "End",
                    "Enter",
                    "F2",
                    "Tab",
                ].includes(e.key)
            ) {
                if (!data.length) return;
                const targetRow = e.shiftKey ? maxRow : 0;
                const targetCol = e.shiftKey ? maxCol : 0;
                focusCell(targetRow, targetCol);
                e.preventDefault();
            }
            return;
        }

        const commitActiveEdit = () => {
            if (editingCell === null) return;
            const [row, col] = editingCell
                .split("-")
                .map((segment) => Number(segment));
            if (Number.isNaN(row) || Number.isNaN(col)) return;
            handleCellBlur(row, col, { suppressRefocus: true });
        };

        const move = (dr, dc) => {
            e.preventDefault();
            if (navWhileEditing) {
                commitActiveEdit();
            }
            focusCell(focusedCell.row + dr, focusedCell.col + dc);
        };

        const moveWithWrap = (dc) => {
            if (data.length === 0) return;
            e.preventDefault();
            if (editingCell !== null) {
                commitActiveEdit();
            }

            let nextRow = focusedCell.row;
            let nextCol = focusedCell.col + dc;

            if (nextCol > maxCol) {
                if (nextRow < maxRow) {
                    nextRow += 1;
                    nextCol = 0;
                } else {
                    nextCol = maxCol;
                }
            } else if (nextCol < 0) {
                if (nextRow > 0) {
                    nextRow -= 1;
                    nextCol = maxCol;
                } else {
                    nextCol = 0;
                }
            }

            focusCell(nextRow, nextCol);
        };

        switch (e.key) {
            case "ArrowUp":
                if (notEditing || navWhileEditing) move(-1, 0);
                break;
            case "ArrowDown":
                if (notEditing || navWhileEditing) move(1, 0);
                break;
            case "ArrowLeft":
                if (notEditing || navWhileEditing) move(0, -1);
                break;
            case "ArrowRight":
                if (notEditing || navWhileEditing) move(0, 1);
                break;
            case "Home":
                if (notEditing || navWhileEditing) {
                    e.preventDefault();
                    if (navWhileEditing) {
                        commitActiveEdit();
                    }
                    focusCell(focusedCell.row, 0);
                }
                break;
            case "End":
                if (notEditing || navWhileEditing) {
                    e.preventDefault();
                    if (navWhileEditing) {
                        commitActiveEdit();
                    }
                    focusCell(focusedCell.row, headers.length - 1);
                }
                break;
            case "Tab": {
                const atStart = focusedCell.row === 0 && focusedCell.col === 0;
                const atEnd =
                    focusedCell.row === maxRow && focusedCell.col === maxCol;

                if ((!e.shiftKey && atEnd) || (e.shiftKey && atStart)) {
                    if (editingCell !== null) {
                        commitActiveEdit();
                    }
                    return; // allow natural tab order outside the grid
                }

                moveWithWrap(e.shiftKey ? -1 : 1);
                break;
            }
            case "Enter":
            case "F2": {
                if (notEditing) {
                    e.preventDefault();
                    handleCellClick(focusedCell.row, focusedCell.col);
                }
                break;
            }
            default:
                break;
        }
    };

    const handleUndoRow = (rowIndex) => {
        if (!originalData[rowIndex]) return;
        const next = [...data];
        next[rowIndex] = originalData[rowIndex].map((cell) => ({
            ...cell,
            isDirty: false,
            isInvalid: false,
        }));
        setData(next);
    };

    const handleDiscardChanges = () => {
        const reverted = originalData.map((row) =>
            row.map((cell) => ({ ...cell, isDirty: false, isInvalid: false })),
        );
        reverted.push(createPlaceholderRow(reverted.length));
        setData(reverted);
        setBackingIds((ids) => ids.slice(0, originalData.length));
        setSaveError(null);
    };

    // ---- save ----
    const handleSaveChanges = async () => {
        setIsSaving(true);
        setSaveError(null);

        try {
            const creates = [];
            const updates = [];

            for (let r = 0; r < data.length - 1; r++) {
                const row = data[r];
                const [name, description, price, amount] = row.map(
                    (c) => c.value,
                );

                const payload = {
                    name: String(name ?? "").trim(),
                    description: String(description ?? "").trim(),
                    price: Number(parsePrice(price)),
                    amount: Number(amount),
                };

                const rowValid =
                    isValidCell(payload.name, 0) &&
                    isValidCell(payload.description, 1) &&
                    isValidCell(String(payload.price), 2) &&
                    isValidCell(String(payload.amount), 3);

                if (!rowValid) {
                    throw new Error(
                        `Row ${r + 1} has invalid data. Fix errors before saving.`,
                    );
                }

                const id = backingIds[r];

                if (id == null) {
                    creates.push(payload);
                } else if (isRowModified(r)) {
                    updates.push({ id, payload });
                }
            }

            // Persist
            if (creates.length) {
                for (const payload of creates) {
                    // Save sequentially to preserve the user-defined row order.
                    // Running POSTs in parallel allowed the backend to reorder items
                    // depending on which request finished first.
                    await saveItem(payload);
                }
            }
            if (updates.length) {
                await Promise.all(
                    updates.map(({ id, payload }) => updateItem(id, payload)),
                );
            }

            // Reload source of truth
            const res = await getAllItems();
            const items = Array.isArray(res?.data) ? res.data : [];
            const rows = items.map((item, idx) => asRow(idx, item));
            const ids = items.map((item) => getItemId(item));
            const working = [...rows, createPlaceholderRow(rows.length)];

            setData(working);
            setOriginalData(rows.map((r) => r.map((c) => ({ ...c }))));
            setBackingIds(ids);
        } catch (e) {
            console.error(e);
            setSaveError(
                e?.response?.data?.message ||
                e?.message ||
                "Failed to save changes. Please try again.",
            );
        } finally {
            setIsSaving(false);
        }
    };

    // ---- derived flags ----
    const hasDirtyChanges = data.some((row, r) =>
        row.some((cell) => !!originalData[r] && cell.isDirty),
    );
    const hasInvalidCells = data.some((row) =>
        row.some((cell) => cell.isInvalid),
    );
    const hasUnsavedRows = data.length - 1 > originalData.length;
    const canDiscard = hasDirtyChanges || hasUnsavedRows;

    return (
        <div className="min-h-full bg-background p-8 overscroll-contain">
            <div className="mx-auto space-y-2">
                <h2 className="text-xl font-medium">Items</h2>
                <div className="flex gap-6">

                    <div className="flex flex-col gap-3">
                        <Card className="overflow-hidden border-2 shadow-none p-0">
                            <div className="overflow-x-auto">
                                <div className="inline-block min-w-full">
                                    {/* Header */}
                                    <div
                                        className="grid bg-muted border-b-2 border-border"
                                        style={{
                                            gridTemplateColumns:
                                                "repeat(4, 180px) 60px",
                                        }}
                                        role="rowgroup"
                                    >
                                        {headers.map((header, colIndex) => (
                                            <div
                                                key={`header-${colIndex}`}
                                                className="border-r border-border h-10 font-medium flex items-center px-5 text-foreground"
                                                role="columnheader"
                                            >
                                                {header}
                                            </div>
                                        ))}
                                        <div className="border-r border-border h-10" />
                                    </div>

                                    {/* Rows */}
                                    <div
                                        className="h-[600px] overflow-y-auto"
                                        onKeyDown={handleGridKeyDown}
                                        onFocus={(e) => {
                                            if (e.target === e.currentTarget) {
                                                if (focusedCell) {
                                                    focusCell(
                                                        focusedCell.row,
                                                        focusedCell.col,
                                                    );
                                                }
                                            }
                                        }}
                                        tabIndex={0}
                                    >
                                        <div
                                            className="grid"
                                            style={{
                                                gridTemplateColumns:
                                                    "repeat(4, 180px) 60px",
                                            }}
                                            role="rowgroup"
                                        >
                                            {data.map((row, rowIndex) => {
                                                const placeholder =
                                                    isPlaceholderRow(rowIndex);
                                                return (
                                                    <React.Fragment
                                                        key={`row-${rowIndex}`}
                                                    >
                                                        {row.map(
                                                            (
                                                                cell,
                                                                colIndex,
                                                            ) => {
                                                                const isEditing =
                                                                    editingCell ===
                                                                    cell.id;
                                                                const isDescriptionColumn =
                                                                    colIndex ===
                                                                    1;

                                                                return (
                                                                    <div
                                                                        key={
                                                                            cell.id
                                                                        }
                                                                        className={`border-b border-r border-border relative ${isDescriptionColumn &&
                                                                            isEditing
                                                                            ? "h-32"
                                                                            : "h-10"
                                                                            }`}
                                                                        role="cell"
                                                                    >
                                                                        {cell.isInvalid && (
                                                                            <div className="absolute left-1 top-1/2 -translate-y-1/2 w-2 h-2 bg-red-500 rounded-full" />
                                                                        )}
                                                                        {cell.isDirty &&
                                                                            !cell.isInvalid && (
                                                                                <div className="absolute left-1 top-1/2 -translate-y-1/2 w-2 h-2 bg-blue-500 rounded-full" />
                                                                            )}

                                                                        {isEditing ? (
                                                                            isDescriptionColumn ? (
                                                                                <textarea
                                                                                    ref={(
                                                                                        el,
                                                                                    ) =>
                                                                                        setCellRef(
                                                                                            el,
                                                                                            rowIndex,
                                                                                            colIndex,
                                                                                        )
                                                                                    }
                                                                                    value={
                                                                                        editValue
                                                                                    }
                                                                                    onChange={(
                                                                                        e,
                                                                                    ) =>
                                                                                        setEditValue(
                                                                                            e
                                                                                                .target
                                                                                                .value,
                                                                                        )
                                                                                    }
                                                                                    onBlur={() =>
                                                                                        handleCellBlur(
                                                                                            rowIndex,
                                                                                            colIndex,
                                                                                        )
                                                                                    }
                                                                                    onKeyDown={(
                                                                                        e,
                                                                                    ) => {
                                                                                        handleGridKeyDown(
                                                                                            e,
                                                                                        );
                                                                                        handleEditorKeyDown(
                                                                                            e,
                                                                                            rowIndex,
                                                                                            colIndex,
                                                                                        );
                                                                                    }}
                                                                                    onFocus={() =>
                                                                                        setFocusedCell(
                                                                                            {
                                                                                                row: rowIndex,
                                                                                                col: colIndex,
                                                                                            },
                                                                                        )
                                                                                    }
                                                                                    tabIndex={
                                                                                        focusedCell?.row ===
                                                                                            rowIndex &&
                                                                                            focusedCell?.col ===
                                                                                            colIndex
                                                                                            ? 0
                                                                                            : -1
                                                                                    }
                                                                                    className="w-full h-full px-3 py-2 bg-accent text-foreground outline-none focus-visible:outline-none focus:ring-2 focus:ring-ring resize-none"
                                                                                    autoFocus
                                                                                />
                                                                            ) : (
                                                                                <input
                                                                                    ref={(
                                                                                        el,
                                                                                    ) =>
                                                                                        setCellRef(
                                                                                            el,
                                                                                            rowIndex,
                                                                                            colIndex,
                                                                                        )
                                                                                    }
                                                                                    type="text"
                                                                                    value={
                                                                                        editValue
                                                                                    }
                                                                                    onChange={(
                                                                                        e,
                                                                                    ) =>
                                                                                        setEditValue(
                                                                                            e
                                                                                                .target
                                                                                                .value,
                                                                                        )
                                                                                    }
                                                                                    onBlur={() =>
                                                                                        handleCellBlur(
                                                                                            rowIndex,
                                                                                            colIndex,
                                                                                        )
                                                                                    }
                                                                                    onKeyDown={(
                                                                                        e,
                                                                                    ) => {
                                                                                        handleGridKeyDown(
                                                                                            e,
                                                                                        );
                                                                                        handleEditorKeyDown(
                                                                                            e,
                                                                                            rowIndex,
                                                                                            colIndex,
                                                                                        );
                                                                                    }}
                                                                                    onFocus={() =>
                                                                                        setFocusedCell(
                                                                                            {
                                                                                                row: rowIndex,
                                                                                                col: colIndex,
                                                                                            },
                                                                                        )
                                                                                    }
                                                                                    tabIndex={
                                                                                        focusedCell?.row ===
                                                                                            rowIndex &&
                                                                                            focusedCell?.col ===
                                                                                            colIndex
                                                                                            ? 0
                                                                                            : -1
                                                                                    }
                                                                                    className="w-full h-full px-3 bg-accent text-foreground outline-none focus-visible:outline-none focus:ring-2 focus:ring-ring"
                                                                                    autoFocus
                                                                                />
                                                                            )
                                                                        ) : (
                                                                            <button
                                                                                ref={(
                                                                                    el,
                                                                                ) =>
                                                                                    setCellRef(
                                                                                        el,
                                                                                        rowIndex,
                                                                                        colIndex,
                                                                                    )
                                                                                }
                                                                                type="button"
                                                                                onClick={() =>
                                                                                    handleCellClick(
                                                                                        rowIndex,
                                                                                        colIndex,
                                                                                    )
                                                                                }
                                                                                onKeyDown={
                                                                                    handleGridKeyDown
                                                                                }
                                                                                onFocus={() =>
                                                                                    setFocusedCell(
                                                                                        {
                                                                                            row: rowIndex,
                                                                                            col: colIndex,
                                                                                        },
                                                                                    )
                                                                                }
                                                                                tabIndex={
                                                                                    focusedCell?.row ===
                                                                                        rowIndex &&
                                                                                        focusedCell?.col ===
                                                                                        colIndex
                                                                                        ? 0
                                                                                        : -1
                                                                                }
                                                                                className={`w-full h-full pl-5 pr-3 text-left transition-colors focus:outline-none focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring ${placeholder
                                                                                    ? "text-muted-foreground italic hover:bg-muted/70"
                                                                                    : "text-foreground hover:bg-muted"
                                                                                    }`}
                                                                            >
                                                                                {cell.value ? (
                                                                                    <span
                                                                                        className={
                                                                                            colIndex ===
                                                                                                1 || colIndex === 0
                                                                                                ? "line-clamp-1"
                                                                                                : ""
                                                                                        }
                                                                                    >
                                                                                        {formatCellValueForDisplay(
                                                                                            cell.value,
                                                                                            colIndex,
                                                                                        )}
                                                                                    </span>
                                                                                ) : (
                                                                                    <span className="text-muted-foreground">
                                                                                        -
                                                                                    </span>
                                                                                )}
                                                                            </button>
                                                                        )}
                                                                    </div>
                                                                );
                                                            },
                                                        )}

                                                        {/* Row actions (only undo for now) */}
                                                        <div className="border-b border-r border-border h-10 flex items-center justify-center">
                                                            {isRowModified(
                                                                rowIndex,
                                                            ) && (
                                                                    <Button
                                                                        variant="ghost"
                                                                        size="icon"
                                                                        onClick={() =>
                                                                            handleUndoRow(
                                                                                rowIndex,
                                                                            )
                                                                        }
                                                                        className="h-8 w-8"
                                                                        title="Undo changes to this row"
                                                                    >
                                                                        <Undo2 className="h-4 w-4" />
                                                                    </Button>
                                                                )}
                                                        </div>
                                                    </React.Fragment>
                                                );
                                            })}
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </Card>
                        <div className="text-sm text-muted-foreground px-2 py-2">
                            <p>
                                <strong>Tip:</strong> Enter to save a cell,
                                Escape to cancel editing.
                            </p>
                            <p className="mt-1">
                                <strong>Name:</strong> Required |{" "}
                                <strong>Description:</strong> Optional |{" "}
                                <strong>Price:</strong> Required (number) |{" "}
                                <strong>Amount:</strong> Required (integer)
                            </p>
                            {hasInvalidCells && (
                                <Alert
                                    variant="destructive"
                                    className="inline-flex items-start gap-2 px-3 py-2 mt-2 w-auto"
                                >
                                    <AlertCircle className="h-4 w-4  shrink-0" />
                                    <AlertTitle>Invalid Data</AlertTitle>
                                    <AlertDescription>
                                        Ensure that all fields are filled
                                        correctly before saving.
                                    </AlertDescription>
                                </Alert>
                            )}
                        </div>
                    </div>
                    <div className="flex flex-col gap-3 w-48 ">
                        <Button
                            onClick={handleSaveChanges}
                            disabled={
                                isSaving ||
                                hasInvalidCells ||
                                (!hasDirtyChanges && !hasUnsavedRows)
                            }
                            size="lg"
                            className="w-full h-14 text-base"
                        >
                            {isSaving ? "Saving..." : "Save Changes"}
                        </Button>
                        <Button
                            variant="outline"
                            onClick={handleDiscardChanges}
                            disabled={!canDiscard || isSaving}
                            size="lg"
                            className="w-full h-14 text-base bg-transparent"
                        >
                            Discard Changes
                        </Button>
                        {saveError && (
                            <p className="text-sm text-red-600 whitespace-pre-line">
                                {saveError}
                            </p>
                        )}
                    </div>
                </div>
            </div>
        </div>
    );
}

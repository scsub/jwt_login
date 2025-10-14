import { useEffect, useState } from "react";
import { getCategories } from "../api/ApiService";

function flattenCategories(categories, level = 0) {
    return categories.flatMap((cat) => [
        { id: cat.id, name: cat.name, level },
        ...(cat.children?.length ? flattenCategories(cat.children, level + 1) : []),
    ]);
}

export default function CategorySelecter({ options, value, onChange, levelFilter = null }) {
    const flat = flattenCategories(options);
    const filtered = levelFilter !== null ? flat.filter((o) => o.level === levelFilter) : flat;

    return (
        <select
            value={value ?? ""}
            onChange={(e) => {
                const v = e.target.value;
                onChange(v === "" ? null : Number(v));
            }}
        >
            <option value=""> 카테고리 </option>
            {filtered.map((opt) => (
                <option key={opt.id} value={opt.id}>
                    {"\u00A0".repeat(opt.level * 2)}
                    {opt.name}
                </option>
            ))}
        </select>
    );
}

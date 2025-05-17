import { useEffect, useState } from "react";
import { getCategories } from "../api/ApiService";

/** 중첩 children 구조를 평탄화 */
function flattenCategories(categories, level = 0) {
    return categories.flatMap((cat) => [
        { id: cat.id, name: cat.name, level },
        ...(cat.children?.length ? flattenCategories(cat.children, level + 1) : []),
    ]);
}

/**
 * props:
 *   - options     : 백엔드에서 받은 중첩된 카테고리 배열
 *   - value       : 선택된 ID
 *   - onChange    : (newId:number|null)=>void
 *   - levelFilter : 보여줄 level (0=대분류,1=중분류,null=전체)
 */
export default function CategorySelecter({ options, value, onChange, levelFilter = null }) {
    // 평탄화
    const flat = flattenCategories(options);
    // levelFilter 적용
    const filtered = levelFilter !== null ? flat.filter((o) => o.level === levelFilter) : flat;

    return (
        <select
            value={value ?? ""}
            onChange={(e) => {
                const v = e.target.value;
                onChange(v === "" ? null : Number(v));
            }}
        >
            <option value="">— 선택하세요 —</option>
            {filtered.map((opt) => (
                <option key={opt.id} value={opt.id}>
                    {"\u00A0".repeat(opt.level * 2)}
                    {opt.name}
                </option>
            ))}
        </select>
    );
}

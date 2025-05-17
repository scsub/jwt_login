import { useEffect, useState } from "react";
import api, { getCategories } from "../api/ApiService";

export default function ProductForm() {
    const MAX_MB = 15;
    const [tree, setTree] = useState([]);
    const [big, setBig] = useState(null);
    const [mid, setMid] = useState(null);
    const [small, setSmall] = useState(null);

    const [images, setImages] = useState([]);

    useEffect(() => {
        getCategories()
            .then((res) => setTree(res.data))
            .catch((e) => {
                console.error(e);
            });
    }, []);

    const mids = big ? big.children : [];
    const smalls = mid ? mid.children : [];

    const handleSubmit = async (e) => {
        try {
            e.preventDefault();
            if (!small) {
                alert("카테고리를 선택하세요");
                return;
            }
            if (images.length === 0) {
                alert("이미지를 선택하세요");
                return;
            }

            //JSON
            const productRequest = {
                name: e.target.name.value,
                description: e.target.desc.value,
                price: Number(e.target.price.value),
                quantity: Number(e.target.qty.value),
                categoryId: small.id,
            };

            //FormData
            const fd = new FormData();
            fd.append(
                "data",
                new Blob([JSON.stringify(productRequest)], {
                    type: "application/json",
                })
            );
            images.forEach((file) => fd.append("files", file));

            await api.post("/products", fd); // Content‑Type 자동으로 multipart/form‑data + boundary

            alert("등록 완료");
        } catch (e) {
            console.log("상품을 등록할수 없습니다", e);
        }
    };

    return (
        <form onSubmit={handleSubmit}>
            <select
                value={big?.id || ""}
                onChange={(e) => {
                    const f = tree.find((c) => c.id === +e.target.value);
                    setBig(f);
                    setMid(null);
                    setSmall(null);
                }}
            >
                <option value="" disabled>
                    대분류
                </option>
                {tree.map((c) => (
                    <option key={c.id} value={c.id}>
                        {c.name}
                    </option>
                ))}
            </select>

            <select
                value={mid?.id || ""}
                disabled={!big}
                onChange={(e) => {
                    const f = mids.find((c) => c.id === +e.target.value);
                    setMid(f);
                    setSmall(null);
                }}
            >
                <option value="" disabled>
                    중분류
                </option>
                {mids.map((c) => (
                    <option key={c.id} value={c.id}>
                        {c.name}
                    </option>
                ))}
            </select>

            <select
                value={small?.id || ""}
                disabled={!mid}
                onChange={(e) => {
                    const f = smalls.find((c) => c.id === +e.target.value);
                    setSmall(f);
                }}
            >
                <option value="" disabled>
                    소분류
                </option>
                {smalls.map((c) => (
                    <option key={c.id} value={c.id}>
                        {c.name}
                    </option>
                ))}
            </select>

            <input name="name" placeholder="상품명" required />
            <input name="desc" placeholder="설명" required />
            <input name="price" type="number" min="0" required />
            <input name="qty" type="number" min="0" required />

            <input
                type="file"
                multiple
                accept="image/*"
                onChange={(e) => {
                    const files = [...e.target.files];
                    const overs = files.filter(
                        (f) => f.size > MAX_MB * 1024 * 1024
                    );
                    if (overs.length) {
                        alert(`파일당 ${MAX_MB}MB 를 넘을 수 없습니다`);
                        return;
                    }
                    setImages(files);
                }}
            />

            <button type="submit">상품 등록</button>
        </form>
    );
}

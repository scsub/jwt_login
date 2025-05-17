import { useEffect, useState } from "react";
import { createCartegotyPost, createCategotyPost, getCategories } from "../api/ApiService";
import { Controller, useForm } from "react-hook-form";
import * as yup from "yup";
import { yupResolver } from "@hookform/resolvers/yup";
import CategorySelecter from "../components/CategorySelecter";

const schema = yup.object({
    level: yup.string().oneOf(["large", "middle", "small"]).required("분류를 선택해주세요"),
    parentId: yup
        .number()
        .nullable()
        .when("level", {
            is: (val) => val !== "large",
            then: (s) => s.required("부모 카테고리를 선택해주세요"),
            otherwise: (s) => s.nullable(),
        }),
    name: yup
        .string()
        .trim()
        .min(1, "이름을 입력해주세요")
        .max(50, "50자 이내로 입력해주세요")
        .required("카테고리 이름을 입력해주세요"),
});

export default function CreateCategoryGPT() {
    const [rawCategories, setRawCategories] = useState([]);
    const {
        control,
        register,
        handleSubmit,
        watch,
        resetField,
        formState: { errors, isSubmitting },
    } = useForm({
        resolver: yupResolver(schema),
        defaultValues: { level: "large", parentId: null, name: "" },
    });

    // 1) 카테고리 트리 불러오기 함수
    const fetchCategories = async () => {
        try {
            const res = await getCategories();
            setRawCategories(res.data);
        } catch (e) {
            console.error("카테고리 로드 실패", e);
            setRawCategories([]);
        }
    };

    // 마운트 시 한 번 로드
    useEffect(() => {
        fetchCategories();
    }, []);

    // level 바꾸면 parentId 초기화
    const level = watch("level");
    useEffect(() => {
        resetField("parentId");
    }, [level, resetField]);

    // level → levelFilter 맵핑
    const levelFilter = level === "middle" ? 0 : level === "small" ? 1 : null;

    // 2) 폼 제출
    const onSubmit = async (data) => {
        try {
            await createCategotyPost({
                name: data.name,
                parentId: data.level === "large" ? null : data.parentId,
            });
            alert("카테고리 생성 완료");
            // 생성 성공 후 **다시 불러오기**
            await fetchCategories();
        } catch (e) {
            console.error(e);
            alert("카테고리 생성에 실패했습니다.");
        }
    };

    return (
        <form onSubmit={handleSubmit(onSubmit)}>
            {/* 분류 종류 */}
            <div>
                <label>분류 종류</label>
                <select {...register("level")}>
                    <option value="large">대분류</option>
                    <option value="middle">중분류</option>
                    <option value="small">소분류</option>
                </select>
                {errors.level && <p className="error">{errors.level.message}</p>}
            </div>

            {/* 부모 카테고리 (대분류 외에만) */}
            {level !== "large" && (
                <div>
                    <label>부모 카테고리</label>
                    <Controller
                        name="parentId"
                        control={control}
                        render={({ field }) => (
                            <CategorySelecter
                                options={rawCategories}
                                value={field.value}
                                onChange={field.onChange}
                                levelFilter={levelFilter}
                            />
                        )}
                    />
                    {errors.parentId && <p className="error">{errors.parentId.message}</p>}
                </div>
            )}

            {/* 새 카테고리 이름 */}
            <div>
                <label>이름</label>
                <input type="text" {...register("name")} />
                {errors.name && <p className="error">{errors.name.message}</p>}
            </div>

            <button type="submit" disabled={isSubmitting}>
                {isSubmitting ? "저장 중…" : "카테고리 생성"}
            </button>
        </form>
    );
}

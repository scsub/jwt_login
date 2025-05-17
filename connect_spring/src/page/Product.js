import { useEffect, useState } from "react";
import { getProducts } from "../api/ApiService";
import { Link } from "react-router-dom";
import { RootUrl } from "../components/path";

function Product() {
    const [products, setProducts] = useState([]);
    const URL = `https://localhost:8443`;
    useEffect(() => {
        fetchProducts();
    }, []);

    const fetchProducts = async () => {
        try {
            const response = await getProducts();
            setProducts(response.data);
        } catch (e) {
            console.error("상품 불러오는 도중 에러 발생");
        }
    };

    return (
        <div>
            <div>물건</div>
            <div>
                <ul>
                    {products.map((product) => (
                        <Link to={`/product/${product.id}`}>
                            <li key={product.id}>
                                <div>이름: {product.name}</div>
                                <div>
                                    사진:
                                    {product.productImageList.map((img) => (
                                        <img
                                            key={img.id}
                                            src={`${RootUrl()}${img.url}`}
                                            style={{ width: "150px", height: "auto" }}
                                        />
                                    ))}
                                </div>
                            </li>
                        </Link>
                    ))}
                </ul>
            </div>
        </div>
    );
}

export default Product;

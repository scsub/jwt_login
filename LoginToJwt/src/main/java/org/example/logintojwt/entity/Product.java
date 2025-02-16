package org.example.logintojwt.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.example.logintojwt.request.ProductRequest;


@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(nullable = false)
    private long price;

    private long quantity;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @Builder
    public Product(String name, String description, long price, long quantity,Category category) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.quantity = quantity;
        this.category = category;
    }

    public void updatePrice(long price) {
        if (price < 0) {
            throw new IllegalArgumentException("가격은 음수가 될수없습니다");
        }
        this.price = price;
    }

    public void updateQuantity(long quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("수량은 음수가 될수없습니다");
        }
        this.quantity = quantity;
    }

    public void updateDescription(String description) {
        this.description = description;
    }
    public void updateName(String name) {
        this.name = name;
    }

    public void updateCategory(Category category) {
        if (this.category != null) {
            this.category.getProducts().remove(this);
        }
        this.category = category;
        category.getProducts().add(this);
    }
    public void updateProduct(ProductRequest productRequest) {
        this.name = productRequest.getName();
        this.description = productRequest.getDescription();
        this.price = productRequest.getPrice();
        this.quantity = productRequest.getQuantity();
    }

    public static Product from(ProductRequest productRequest, Category category) {
        return Product.builder()
                .name(productRequest.getName())
                .description(productRequest.getDescription())
                .price(productRequest.getPrice())
                .quantity(productRequest.getQuantity())
                .category(category)
                .build();
    }
}

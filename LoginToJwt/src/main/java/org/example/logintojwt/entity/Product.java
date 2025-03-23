package org.example.logintojwt.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.example.logintojwt.request.ProductRequest;

import java.util.ArrayList;
import java.util.List;


@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
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

    @Builder.Default
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductImage> images = new ArrayList<>();

    @Builder.Default
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Review> reviewList = new ArrayList<>();

    public void addImage(ProductImage image) {
        images.add(image);
        image.updateProduct(this);
    }

    public void removeImage(ProductImage image) {
        images.remove(image);
        image.updateProduct(null);
    }
    public void addReview(Review review) {
        reviewList.add(review);
        review.updateProduct(this);
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

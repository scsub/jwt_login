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
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(nullable = false)
    private Long price;

    private Long quantity;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @JsonIgnore
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Review> reviewList;

    @JsonIgnore
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductImage> productImageList;

    @Builder
    public Product(Long id, String name, String description, Long price, Long quantity, Category category) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.quantity = quantity;
        this.category = category;
        this.reviewList = new ArrayList<>();
        this.productImageList = new ArrayList<>();
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

    public void addImage(ProductImage image) {
        productImageList.add(image);
        image.updateProduct(this);
    }

    public void removeImage(ProductImage image) {
        productImageList.remove(image);
        image.updateProduct(null);
    }
    public void addReview(Review review) {
        reviewList.add(review);
        review.updateProduct(this);
    }

    public void updatePrice(Long price) {
        if (price < 0) {
            throw new IllegalArgumentException("가격은 음수가 될수없습니다");
        }
        this.price = price;
    }

    public void updateQuantity(Long quantity) {
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

    public void minusQuantity(Long quantity) {
        this.quantity -= quantity;
    }

    public void plusQuantity(Long quantity) {
        this.quantity += quantity;
    }

    public void updateProduct(ProductRequest productRequest) {
        this.name = productRequest.getName();
        this.description = productRequest.getDescription();
        this.price = productRequest.getPrice();
        this.quantity = productRequest.getQuantity();
    }



}

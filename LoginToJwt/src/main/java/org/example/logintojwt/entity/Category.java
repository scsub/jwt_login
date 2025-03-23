package org.example.logintojwt.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Category parent;

    @Builder.Default
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "parent", orphanRemoval = true, cascade = CascadeType.ALL)
    private List<Category> children = new ArrayList<>();

    @Builder.Default
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "category")
    private List<Product> products = new ArrayList<>();

    public void addChild(Category child) {
        children.add(child);
        child.parent = this;
    }

    public void addProduct(Product product) {
        products.add(product);
        product.updateCategory(this);
    }

    public void updateCategoryName(String name) {
        this.name = name;
    }


}

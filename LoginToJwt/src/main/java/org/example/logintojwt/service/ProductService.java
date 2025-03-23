package org.example.logintojwt.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.logintojwt.entity.Category;
import org.example.logintojwt.entity.Product;
import org.example.logintojwt.exception.ProductNotFoundException;
import org.example.logintojwt.repository.CategoryRepository;
import org.example.logintojwt.repository.ProductRepository;
import org.example.logintojwt.request.ProductRequest;
import org.example.logintojwt.response.ProductResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public ProductResponse createProduct(ProductRequest productRequest) {
        Category category = categoryRepository.findById(productRequest.getCategoryId()).orElseThrow(() -> new IllegalArgumentException("이미 카테고리가 있음"));
        Product product = Product.from(productRequest, category);
        productRepository.save(product);
        ProductResponse productResponse = ProductResponse.from(product);
        return productResponse;
    }

    public List<ProductResponse> findAllProduct() {
        List<Product> productList = productRepository.findAll();
        return productList.stream().map(product -> ProductResponse.from(product)).collect(Collectors.toList());
    }

    public ProductResponse findProductById(Long id) {
        Product product = productRepository.findById(id).orElseThrow(() -> new ProductNotFoundException("상품을 찾을수 없음"));
        return ProductResponse.from(product);
    }

    public void updateProduct(Long id, ProductRequest productRequest) {
        Product product = productRepository.findById(id).orElseThrow(() -> new ProductNotFoundException("상품을 찾을수 없음"));

        if (productRequest.getName() != null) {
            product.updateName(productRequest.getName());
        }
        if (productRequest.getDescription() != null) {
            product.updateDescription(productRequest.getDescription());
        }
        if (productRequest.getPrice() != null) {
            product.updatePrice(productRequest.getPrice());
        }
        if (productRequest.getQuantity() != null) {
            product.updateQuantity(productRequest.getQuantity());
        }
        productRepository.save(product);
    }

    public void updateProductCategory(ProductRequest productRequest,Long id) {
        Product product = productRepository.findById(id).orElseThrow(() -> new ProductNotFoundException("상품을 찾을수 없음"));
        Long categoryId = productRequest.getCategoryId();
        Category category = categoryRepository.findById(categoryId).orElseThrow(() -> new IllegalArgumentException("카테고리를 찾을수 없음"));
        product.updateCategory(category);
    }

    public List<ProductResponse> searchProductByName(String name) {
        List<Product> productList = productRepository.findByNameContaining(name);
        List<ProductResponse> productResponseList = productList.stream()
                .map(product -> ProductResponse.from(product))
                .collect(Collectors.toList());
        return productResponseList;
    }

    public void deleteProductById(Long id) {
        productRepository.deleteById(id);
    }
}

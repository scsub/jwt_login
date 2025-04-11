package org.example.logintojwt.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.logintojwt.entity.Category;
import org.example.logintojwt.entity.Product;
import org.example.logintojwt.entity.ProductImage;
import org.example.logintojwt.exception.ProductNotFoundException;
import org.example.logintojwt.repository.CategoryRepository;
import org.example.logintojwt.repository.ProductImageRepository;
import org.example.logintojwt.repository.ProductRepository;
import org.example.logintojwt.request.ProductRequest;
import org.example.logintojwt.response.ProductResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ProductService {
    @Value("${file.upload-dir}")
    private String uploadDir;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductImageRepository productImageRepository;

    public ProductResponse createProduct(ProductRequest productRequest, List<MultipartFile> files) {
        Category category = categoryRepository.findById(productRequest.getCategoryId()).orElseThrow(() -> new IllegalArgumentException("카테고리가 없음"));
        Product product = Product.from(productRequest, category);
        saveImages(product, files);
        productRepository.save(product);
        return ProductResponse.from(product);
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

    private void saveImages(Product product, List<MultipartFile> files) {
        for (MultipartFile file : files) {
            if (file.isEmpty()) continue; // 파일없으면 그 다음 파일로
            String savedPath = storeFile(file); // 반환된 저장 경로

            ProductImage image = ProductImage.builder()
                    .url(savedPath)
                    .product(product)
                    .build();

            productImageRepository.save(image);
            product.addImage(image);
        }
    }

    private String storeFile(MultipartFile file) {
        String originalFilename = file.getOriginalFilename(); // 파일 이름 추출
        String extension = StringUtils.getFilenameExtension(originalFilename); // 그 이름으로 확장자 추출
        String uuid = UUID.randomUUID().toString(); // UUID 생성
        String fileName = uuid + "." + extension; // UUID + 확장자로 중복되지 않는 파일명 만들기

        try {
            Path path = Paths.get(uploadDir + fileName); // 폴더 경로에 파일을 저장 시킴 ex)  files/UUID.gif
            file.transferTo(path.toFile());
            return "/images/" + fileName; // 노출될 경로만 기록함
        }catch (IOException e){
            throw new RuntimeException("파일 저장 실패", e);
        }
    }
}

package org.example.logintojwt.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.logintojwt.request.ProductRequest;
import org.example.logintojwt.response.ProductResponse;
import org.example.logintojwt.response.SuccessResponse;
import org.example.logintojwt.service.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
public class ProductController {
    private final ProductService productService;

    @PostMapping
    public ResponseEntity<ProductResponse> addProduct(
            @RequestPart("data") ProductRequest productRequest,
            @RequestPart("files") List<MultipartFile> files) {
        log.info("데이터와 이미지 확인");
        ProductResponse productResponse = productService.createProduct(productRequest, files);
        return ResponseEntity.status(HttpStatus.CREATED).body(productResponse);
    }

    @GetMapping
    public ResponseEntity<?> getAllProducts() {
        List<ProductResponse> allProduct = productService.findAllProduct();
        return ResponseEntity.status(HttpStatus.OK).body(allProduct);
    }

    @GetMapping("/{productId}")
    public ResponseEntity<?> getProduct(@PathVariable Long productId) {
        ProductResponse productResponse = productService.findProductById(productId);
        return ResponseEntity.status(HttpStatus.OK).body(productResponse);
    }

    @GetMapping("/search") // ?query=물건이름  네이버에서 이렇게 사용
    public ResponseEntity<?> searchProductsByName(@RequestParam String query) {
        List<ProductResponse> productResponseList = productService.searchProductByName(query);
        return ResponseEntity.status(HttpStatus.OK).body(productResponseList);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> updateProduct(@PathVariable Long id, @RequestBody ProductRequest productRequest) {
        productService.updateProduct(id, productRequest);
        SuccessResponse successResponse = new SuccessResponse("상품 업데이트돰");
        return ResponseEntity.status(HttpStatus.OK).body(successResponse);
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<?> deleteProduct(@PathVariable Long productId) {
        productService.deleteProductById(productId);
        SuccessResponse successResponse = new SuccessResponse("상품 삭제됨");
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(successResponse);
    }
}

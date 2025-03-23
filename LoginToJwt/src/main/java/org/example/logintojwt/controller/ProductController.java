package org.example.logintojwt.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.logintojwt.request.ProductRequest;
import org.example.logintojwt.response.ProductResponse;
import org.example.logintojwt.response.SuccessResponse;
import org.example.logintojwt.service.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
public class ProductController {
    private final ProductService productService;

    @PostMapping
    public ResponseEntity<ProductResponse> addProduct(@RequestBody ProductRequest productRequest) {
        ProductResponse productResponse = productService.createProduct(productRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(productResponse);
    }

    @GetMapping
    public ResponseEntity<?> getAllProducts() {
        List<ProductResponse> allProduct = productService.findAllProduct();
        return ResponseEntity.status(HttpStatus.OK).body(allProduct);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getProduct(@PathVariable Long id) {
        ProductResponse productResponse = productService.findProductById(id);
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


    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id) {
        productService.deleteProductById(id);
        SuccessResponse successResponse = new SuccessResponse("상품 삭제됨");
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(successResponse);
    }
}

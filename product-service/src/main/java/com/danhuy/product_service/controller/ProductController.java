package com.danhuy.product_service.controller;

import com.danhuy.product_service.dto.ProductRequest;
import com.danhuy.product_service.dto.ProductResponse;
import com.danhuy.product_service.service.ProductService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Slf4j
public class ProductController {

  private final ProductService productService;

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public ResponseEntity<ProductResponse> createProduct(
      @Valid @RequestBody ProductRequest productRequest) {
    log.info("Received request to create product: {}", productRequest.getName());

    ProductResponse createdProduct = productService.createProduct(productRequest);
    return new ResponseEntity<>(createdProduct, HttpStatus.CREATED);
  }

  @PutMapping("/{id}")
  public ResponseEntity<ProductResponse> updateProduct(
      @PathVariable Long id,
      @Valid @RequestBody ProductRequest productRequest) {
    log.info("Received request to update product with id: {}", id);

    ProductResponse updatedProduct = productService.updateProduct(id, productRequest);
    return ResponseEntity.ok(updatedProduct);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
    log.info("Received request to delete product with id: {}", id);

    productService.deleteProduct(id);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/{id}")
  @CircuitBreaker(name = "productService", fallbackMethod = "getProductFallback")
  @Retry(name = "productService")
  @TimeLimiter(name = "productService")
  public CompletableFuture<ResponseEntity<ProductResponse>> getProduct(@PathVariable Long id) {
    log.info("Received request to get product with id: {}", id);

    return CompletableFuture.supplyAsync(() -> {
      try {
        ProductResponse product = productService.getProductById(id);
        return ResponseEntity.ok(product);
      } catch (EntityNotFoundException e) {
        return ResponseEntity.notFound().build();
      }
    });
  }

  public CompletableFuture<ResponseEntity<String>> getProductFallback(Long id, Exception e) {
    log.error("Fallback for getProduct with id: {}. Error: {}", id, e.getMessage());

    return CompletableFuture.supplyAsync(() ->
        ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
            .body("Service is currently unavailable. Please try again later.")
    );
  }

  @GetMapping
  public ResponseEntity<Page<ProductResponse>> getAllProducts(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(defaultValue = "id") String sort) {
    log.info("Received request to get all products with page: {}, size: {}", page, size);

    Pageable pageable = PageRequest.of(page, size, Sort.by(sort));
    Page<ProductResponse> products = productService.getAllProducts(pageable);

    return ResponseEntity.ok(products);
  }

  @GetMapping("/search")
  public ResponseEntity<List<ProductResponse>> searchProducts(
      @RequestParam String name) {
    log.info("Received request to search products by name: {}", name);

    List<ProductResponse> products = productService.searchProductsByName(name);
    return ResponseEntity.ok(products);
  }

  @GetMapping("/category/{categoryId}")
  public ResponseEntity<List<ProductResponse>> getProductsByCategory(
      @PathVariable Long categoryId) {
    log.info("Received request to get products by category id: {}", categoryId);

    List<ProductResponse> products = productService.getProductsByCategory(categoryId);
    return ResponseEntity.ok(products);
  }

  @GetMapping("/advanced-search")
  public ResponseEntity<Page<ProductResponse>> advancedSearch(
      @RequestParam(required = false) String name,
      @RequestParam(required = false) Long categoryId,
      @RequestParam(required = false) BigDecimal minPrice,
      @RequestParam(required = false) BigDecimal maxPrice,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(defaultValue = "id") String sort) {

    log.info(
        "Received request for advanced search with name: {}, category: {}, price range: {} - {}",
        name, categoryId, minPrice, maxPrice);

    Pageable pageable = PageRequest.of(page, size, Sort.by(sort));
    Page<ProductResponse> products = productService.searchProducts(name, categoryId, minPrice,
        maxPrice, pageable);

    return ResponseEntity.ok(products);
  }
}

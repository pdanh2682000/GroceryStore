package com.danhuy.product_service.service;

import com.danhuy.common_service.const_enum.MessageEnum;
import com.danhuy.common_service.exception.ex.AppException;
import com.danhuy.product_service.cache.ProductCacheService;
import com.danhuy.product_service.dto.ProductRequest;
import com.danhuy.product_service.dto.ProductResponse;
import com.danhuy.product_service.entity.Category;
import com.danhuy.product_service.entity.Product;
import com.danhuy.product_service.event.producer.ProductEventProducer;
import com.danhuy.product_service.repository.ProductRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements ProductService {

  private final ProductRepository productRepository;
  private final ProductCacheService productCacheService;
  private final ProductEventProducer productEventProducer;

  /**
   * Create a new product - send product created event - cache the product
   *
   * @param productRequest ProductRequest
   * @return ProductResponse
   */
  @Override
  @Transactional
  public ProductResponse createProduct(ProductRequest productRequest) {
    log.info("Creating new product with name: {}", productRequest.getName());

    // Check if product with same name already exists
    if (productRepository.existsByNameIgnoreCase(productRequest.getName())) {
      throw new AppException(MessageEnum.PRODUCT_EXISTED);
    }

    // Create a new product entity
    Product product = mapToEntity(productRequest);
    Product savedProduct = productRepository.save(product);

    // Publish product created event
    productEventProducer.publishProductCreatedEvent(savedProduct);

    ProductResponse response = mapToDto(savedProduct);

    // Cache the product
    productCacheService.cacheProduct(response);

    return response;
  }

  /**
   * Update an existing product - send product updated event - evict the product from the cache
   *
   * @param id             Long
   * @param productRequest ProductRequest
   * @return ProductResponse
   */
  @Override
  @Transactional
  public ProductResponse updateProduct(Long id, ProductRequest productRequest) {
    log.info("Updating product with id: {}", id);

    Product product = productRepository.findById(id)
        .orElseThrow(() -> new AppException(MessageEnum.PRODUCT_NOT_EXISTED));

    // Update product fields
    product.setName(productRequest.getName());
    product.setDescription(productRequest.getDescription());
    product.setPrice(productRequest.getPrice());
    product.setStock(productRequest.getStock());
    product.setExpiry(productRequest.getExpiry());
    // Set category if provided
    if (productRequest.getCategoryId() != null) {
      Category category = new Category();
      category.setId(productRequest.getCategoryId());
      product.setCategory(category);
    }
    product.setImageUrl(productRequest.getImageUrl());

    // Update product entity
    Product updatedProduct = productRepository.save(product);

    // Publish product updated event
    productEventProducer.publishProductUpdatedEvent(updatedProduct);

    // Evict product from the cache
    productCacheService.evictProductCache(id);

    return mapToDto(updatedProduct);
  }

  /**
   * Delete an existing product - send product deleted event - evict all products from the cache
   *
   * @param id Long
   */
  @Override
  @Transactional
  public void deleteProduct(Long id) {
    log.info("Deleting product with id: {}", id);

    Product product = productRepository.findById(id)
        .orElseThrow(() -> new AppException(MessageEnum.PRODUCT_NOT_EXISTED));

    // Soft delete - mark as inactive
    product.setActive(false);
    Product deletedProduct = productRepository.save(product);

    // Publish product deleted event
    productEventProducer.publishProductDeletedEvent(deletedProduct);

    // Evict all the product from the cache
    productCacheService.evictAllProductCaches();
  }

  /**
   * Get a product by id - try to get from the cache first - if not in cache, get from the DB
   *
   * @param id Long
   * @return ProductResponse
   */
  @Override
  @Transactional(readOnly = true)
  public ProductResponse getProductById(Long id) {
    log.info("Fetching product with id: {}", id);

    // Try to get from the cache first
    ProductResponse cachedProduct = productCacheService.getProductFromCache(id);

    if (cachedProduct != null) {
      log.info("Product found in cache: {}", id);
      return cachedProduct;
    }

    // If not in cache, get from the repository
    Product product = productRepository.findById(id)
        .orElseThrow(() -> new AppException(MessageEnum.PRODUCT_NOT_EXISTED));

    if (!product.isActive()) {
      throw new AppException(MessageEnum.PRODUCT_NOT_EXISTED);
    }

    ProductResponse response = mapToDto(product);

    // Cache the product
    productCacheService.cacheProduct(response);

// code to test case timeout with circuitbreaker
//    try {
//      Thread.sleep(3000);
//    } catch (InterruptedException e) {
//      throw new RuntimeException(e);
//    }
    return response;
  }

  /**
   * Get all products - try to get from the cache first - if not in cache, get from the DB
   *
   * @param pageable Pageable
   * @return Page<ProductResponse>
   */
  @Override
  @Transactional(readOnly = true)
  public Page<ProductResponse> getAllProducts(Pageable pageable) {
    log.info("Fetching all products with pagination");

    return productRepository.findByIsActiveTrue().isEmpty()
        ? Page.empty(pageable)
        : productRepository.findAll(pageable)
            .map(this::mapToDto);
  }

  /**
   * Search products by name - try to get from the cache first - if not in cache, search in the DB
   *
   * @param name String
   * @return List<ProductResponse>
   */
  @Override
  @Transactional(readOnly = true)
  public List<ProductResponse> searchProductsByName(String name) {
    log.info("Searching products by name: {}", name);

    // Try to get from the cache first
    List<ProductResponse> cachedResults = productCacheService.getSearchResultsFromCache(name);

    if (cachedResults != null) {
      log.info("Search results found in cache for name: {}", name);
      return cachedResults;
    }

    List<ProductResponse> results = productRepository.findByNameContainingIgnoreCase(name)
        .stream()
        .filter(Product::isActive)
        .map(this::mapToDto)
        .collect(Collectors.toList());

    // Cache the results
    productCacheService.cacheSearchResults(name, results);

    return results;
  }

  /**
   * Get products by category id - try to get from the cache first - if not in cache, get from the
   * DB
   *
   * @param categoryId Long
   * @return List<ProductResponse>
   */
  @Override
  @Transactional(readOnly = true)
  public List<ProductResponse> getProductsByCategory(Long categoryId) {
    log.info("Fetching products by category id: {}", categoryId);

    // Try to get from the cache first
    List<ProductResponse> cachedResults = productCacheService.getProductsByCategoryFromCache(
        categoryId);

    if (cachedResults != null) {
      log.info("Products found in cache for category: {}", categoryId);
      return cachedResults;
    }

    List<ProductResponse> results = productRepository.findByCategoryId(categoryId)
        .stream()
        .filter(Product::isActive)
        .map(this::mapToDto)
        .collect(Collectors.toList());

    // Cache the results
    productCacheService.cacheProductsByCategory(categoryId, results);

    return results;
  }

  /**
   * Advanced search products - try to get from the cache first - if not in cache, search in the DB
   *
   * @param name       String
   * @param categoryId Long
   * @param minPrice   BigDecimal
   * @param maxPrice   BigDecimal
   * @param pageable   Pageable
   * @return Page<ProductResponse>
   */
  @Override
  @Transactional(readOnly = true)
  public Page<ProductResponse> searchProducts(
      String name,
      Long categoryId,
      BigDecimal minPrice,
      BigDecimal maxPrice,
      Pageable pageable) {
    log.info("Advanced search for products with name: {}, categoryId: {}, price range: {} - {}",
        name, categoryId, minPrice, maxPrice);

    return productRepository.searchProducts(name, categoryId, minPrice, maxPrice, pageable)
        .map(this::mapToDto);
  }

  // Helper methods
  private Product mapToEntity(ProductRequest productRequest) {
    Product product = new Product();
    product.setName(productRequest.getName());
    product.setDescription(productRequest.getDescription());
    product.setPrice(productRequest.getPrice());
    product.setImageUrl(productRequest.getImageUrl());
    product.setStock(productRequest.getStock());
    product.setExpiry(productRequest.getExpiry());
    product.setPhoneSupplier(productRequest.getPhoneSupplier());

    // Set category if provided
    if (productRequest.getCategoryId() != null) {
      Category category = new Category();
      category.setId(productRequest.getCategoryId());
      product.setCategory(category);
    }

    return product;
  }

  private ProductResponse mapToDto(Product product) {
    ProductResponse.CategoryDto categoryDto = null;

    if (product.getCategory() != null) {
      categoryDto = ProductResponse.CategoryDto.builder()
          .id(product.getCategory().getId())
          .name(product.getCategory().getName())
          .build();
    }

    return ProductResponse.builder()
        .id(product.getId())
        .name(product.getName())
        .description(product.getDescription())
        .price(product.getPrice())
        .category(categoryDto)
        .imageUrl(product.getImageUrl())
        .createdAt(product.getCreatedAt())
        .updatedAt(product.getUpdatedAt())
        .isActive(product.isActive())
        .stock(product.getStock())
        .expiry(product.getExpiry())
        .phoneSupplier(product.getPhoneSupplier())
        .build();
  }
}

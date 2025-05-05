package com.danhuy.product_service.cache;

import com.danhuy.product_service.dto.ProductResponse;
import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductCacheService {

  private static final String PRODUCT_KEY_PREFIX = "product:";
  private static final String PRODUCTS_LIST_KEY = "products:all";
  private static final String PRODUCTS_BY_CATEGORY_PREFIX = "products:category:";
  private static final String PRODUCTS_SEARCH_PREFIX = "products:search:";
  private static final long DEFAULT_TTL = 3600; // 1 hour in seconds

  private final RedisTemplate<String, Object> redisTemplate;

  /**
   * cache for product detail
   *
   * @param product ProductResponse
   */
  public void cacheProduct(ProductResponse product) {
    // Ex: `product:1`
    String key = PRODUCT_KEY_PREFIX + product.getId();

    redisTemplate.opsForValue().set(key, product, DEFAULT_TTL, TimeUnit.SECONDS);
    log.debug("Product cached with key: {}", key);
  }

  /**
   * get product detail from cache
   *
   * @param productId Long
   * @return ProductResponse
   */
  public ProductResponse getProductFromCache(Long productId) {
    // Ex: `product:1`
    String key = PRODUCT_KEY_PREFIX + productId;

    ProductResponse product = (ProductResponse) redisTemplate.opsForValue().get(key);

    if (product != null) {
      log.debug("Product found in cache with key: {}", key);
      return product;
    }

    log.debug("Product not found in cache with key: {}", key);
    return null;
  }

  /**
   * cache all products
   *
   * @param products List<ProductResponse>
   */
  public void cacheProductsList(List<ProductResponse> products) {
    // for cache all products
    redisTemplate.opsForValue().set(PRODUCTS_LIST_KEY, products, DEFAULT_TTL, TimeUnit.SECONDS);
    log.debug("Products list cached with key: {}", PRODUCTS_LIST_KEY);
  }

  /**
   * get all products from the cache
   *
   * @return List<ProductResponse>
   */
  @SuppressWarnings("unchecked")
  public List<ProductResponse> getProductsListFromCache() {
    List<ProductResponse> products = (List<ProductResponse>) redisTemplate.opsForValue()
        .get(PRODUCTS_LIST_KEY);

    if (products != null) {
      log.debug("Products list found in cache");
      return products;
    }

    log.debug("Products list not found in cache");
    return null;
  }

  /**
   * cache products by category
   *
   * @param categoryId Long
   * @param products   List<ProductResponse>
   */
  public void cacheProductsByCategory(Long categoryId, List<ProductResponse> products) {
    // Ex: `products:category:1`
    String key = PRODUCTS_BY_CATEGORY_PREFIX + categoryId;

    redisTemplate.opsForValue().set(key, products, DEFAULT_TTL, TimeUnit.SECONDS);
    log.debug("Products by category cached with key: {}", key);
  }

  /**
   * get products by category from the cache
   *
   * @param categoryId Long
   * @return List<ProductResponse>
   */
  @SuppressWarnings("unchecked")
  public List<ProductResponse> getProductsByCategoryFromCache(Long categoryId) {
    // Ex: `products:category:1`
    String key = PRODUCTS_BY_CATEGORY_PREFIX + categoryId;

    List<ProductResponse> products = (List<ProductResponse>) redisTemplate.opsForValue().get(key);

    if (products != null) {
      log.debug("Products by category found in cache with key: {}", key);
      return products;
    }

    log.debug("Products by category not found in cache with key: {}", key);
    return null;
  }

  /**
   * cache search results
   *
   * @param searchKey String
   * @param products  List<ProductResponse>
   */
  public void cacheSearchResults(String searchKey, List<ProductResponse> products) {
    // Ex: `products:search:name=Apple`
    String key = PRODUCTS_SEARCH_PREFIX + searchKey.toLowerCase().replace(" ", "_");

    redisTemplate.opsForValue().set(key, products, DEFAULT_TTL, TimeUnit.SECONDS);
    log.debug("Search results cached with key: {}", key);
  }

  /**
   * get search results from the cache
   *
   * @param searchKey String
   * @return List<ProductResponse>
   */
  @SuppressWarnings("unchecked")
  public List<ProductResponse> getSearchResultsFromCache(String searchKey) {
    // Ex: `products:search:name=Apple`
    String key = PRODUCTS_SEARCH_PREFIX + searchKey.toLowerCase().replace(" ", "_");

    List<ProductResponse> products = (List<ProductResponse>) redisTemplate.opsForValue().get(key);

    if (products != null) {
      log.debug("Search results found in cache with key: {}", key);
      return products;
    }

    log.debug("Search results not found in cache with key: {}", key);
    return null;
  }

  /**
   * evict product cache by product id
   *
   * @param productId Long
   */
  public void evictProductCache(Long productId) {
    String key = PRODUCT_KEY_PREFIX + productId;
    redisTemplate.delete(key);
    // Cũng xóa các cache danh sách để đảm bảo dữ liệu nhất quán
    redisTemplate.delete(PRODUCTS_LIST_KEY);
    log.debug("Product cache evicted for key: {}", key);
  }

  /**
   * evict all product caches
   */
  public void evictAllProductCaches() {
    // Xóa tất cả các khóa với pattern tương ứng
    redisTemplate.delete(redisTemplate.keys(PRODUCT_KEY_PREFIX + "*"));
    redisTemplate.delete(redisTemplate.keys(PRODUCTS_BY_CATEGORY_PREFIX + "*"));
    redisTemplate.delete(redisTemplate.keys(PRODUCTS_SEARCH_PREFIX + "*"));
    redisTemplate.delete(PRODUCTS_LIST_KEY);
    log.debug("All product caches evicted");
  }

}

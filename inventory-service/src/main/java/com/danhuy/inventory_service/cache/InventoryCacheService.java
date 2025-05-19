package com.danhuy.inventory_service.cache;

import com.danhuy.inventory_service.dto.InventoryResponse;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryCacheService {

  private static final String INVENTORY_KEY_PREFIX = "inventory:";
  private static final long DEFAULT_TTL = 3600;

  private final RedisTemplate<String, Object> redisTemplate;

  /**
   * cache for inventory information of a product.
   *
   * @param inventoryResponse InventoryResponse
   */
  public void cacheInventory(InventoryResponse inventoryResponse) {
    // Ex: `inventory:1`
    String key = INVENTORY_KEY_PREFIX + inventoryResponse.getProductId();

    redisTemplate.opsForValue().set(key, inventoryResponse, DEFAULT_TTL, TimeUnit.SECONDS);
    log.debug("Inventory cached with key: {}", key);
  }

  /**
   * get inventory information of a product from the cache
   *
   * @param productId Long
   * @return InventoryResponse
   */
  public InventoryResponse getInventoryFromCache(Long productId) {
    // Ex: `inventory:1`
    String key = INVENTORY_KEY_PREFIX + productId;

    InventoryResponse response = (InventoryResponse) redisTemplate.opsForValue().get(key);

    if (response != null) {
      log.debug("Inventory found in cache with key: {}", key);
      return response;
    }

    log.debug("Inventory not found in cache with key: {}", key);
    return null;
  }

  /**
   * evict inventory cache by product id
   *
   * @param productId Long
   */
  public void evictInventoryCache(Long productId) {
    String key = INVENTORY_KEY_PREFIX + productId;
    redisTemplate.delete(key);
    log.debug("Inventory cache evicted for key: {}", key);
  }

}

package com.danhuy.inventory_service.service.impl;

import static com.danhuy.common_service.enums.InventoryUpdateType.COMMIT;
import static com.danhuy.common_service.enums.InventoryUpdateType.RELEASE;
import static com.danhuy.common_service.enums.InventoryUpdateType.RESERVE;

import com.danhuy.common_service.dto.OrderItemDto;
import com.danhuy.common_service.enums.MessageEnum;
import com.danhuy.common_service.event.inventory.InventoryCheckEvent;
import com.danhuy.common_service.event.inventory.InventoryCheckResultEvent;
import com.danhuy.common_service.event.inventory.InventoryUpdateEvent;
import com.danhuy.common_service.event.inventory.InventoryUpdateResultEvent;
import com.danhuy.common_service.exception.ex.AppException;
import com.danhuy.inventory_service.cache.InventoryCacheService;
import com.danhuy.inventory_service.dto.InventoryRequest;
import com.danhuy.inventory_service.dto.InventoryResponse;
import com.danhuy.inventory_service.entity.Inventory;
import com.danhuy.inventory_service.repository.InventoryRepository;
import com.danhuy.inventory_service.service.InventoryService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
public class InventoryServiceImpl implements InventoryService {

  private final InventoryRepository inventoryRepository;
  private final InventoryCacheService inventoryCacheService;

  // ***** API *****

  /**
   * Create new inventory for product.
   *
   * @param inventoryRequest InventoryRequest
   * @return InventoryResponse
   */
  @Override
  @Transactional
  public InventoryResponse createInventory(InventoryRequest inventoryRequest) {
    if (inventoryRepository.findByProductId(inventoryRequest.getProductId()).isPresent()) {
      throw new AppException(MessageEnum.INVENTORY_EXISTED, inventoryRequest.getProductId());
    }

    Inventory inventory = Inventory.builder()
        .productId(inventoryRequest.getProductId())
        .quantity(inventoryRequest.getQuantity())
        .reservedQuantity(0)
        .version(1L)
        .build();

    Inventory savedInventory = inventoryRepository.save(inventory);
    return mapToResponse(savedInventory);
  }

  /**
   * Update quantity in inventory for product. Besides, remove data in caching
   *
   * @param productId        Long
   * @param inventoryRequest InventoryRequest
   * @return InventoryResponse
   */
  @Override
  @Transactional
  public InventoryResponse updateInventory(Long productId, InventoryRequest inventoryRequest) {
    Inventory inventory = inventoryRepository.findByProductId(productId)
        .orElseThrow(
            () -> new AppException(MessageEnum.INVENTORY_NOT_EXISTED, productId));

    inventory.setQuantity(inventoryRequest.getQuantity());

    // Make sure reserved quantity is not more than total quantity
    if (inventory.getReservedQuantity() > inventory.getQuantity()) {
      inventory.setReservedQuantity(inventory.getQuantity());
    }

    Inventory updatedInventory = inventoryRepository.save(inventory);
    // Evict inventory from the cache
    inventoryCacheService.evictInventoryCache(productId);
    return mapToResponse(updatedInventory);
  }

  /**
   * Get inventory information of the product.
   *
   * @param productId Long
   * @return InventoryResponse
   */
  @Override
  public InventoryResponse getInventoryByProductId(Long productId) {
    // Try to get from the cache first
    InventoryResponse cachedInventory = inventoryCacheService.getInventoryFromCache(productId);

    if (cachedInventory != null) {
      log.info("Inventory found in cache: {}", productId);
      return cachedInventory;
    }

    Inventory inventory = inventoryRepository.findByProductId(productId)
        .orElseThrow(
            () -> new AppException(MessageEnum.INVENTORY_NOT_EXISTED, productId));

    InventoryResponse response = mapToResponse(inventory);
    // Cache the inventory information
    inventoryCacheService.cacheInventory(response);
    return response;
  }

  /**
   * Get all inventory information of the product.
   *
   * @param pageable Pageable
   * @return Page<InventoryResponse>
   */
  @Override
  public Page<InventoryResponse> getAllInventory(Pageable pageable) {
    return inventoryRepository.findAll(pageable)
        .map(this::mapToResponse);
  }

  /**
   * Check the available quantity of the product.
   *
   * @param productId Long
   * @param quantity  Integer
   * @return true if available, otherwise
   */
  @Override
  public boolean isInStock(Long productId, Integer quantity) {
    return inventoryRepository.findByProductId(productId)
        .map(inventory -> inventory.hasAvailableQuantity(quantity))
        .orElse(false);
  }

  /**
   * Get all inventory information if quantity below a threshold
   *
   * @param threshold Integer
   * @return List<InventoryResponse>
   */
  @Override
  public List<InventoryResponse> getLowStockProducts(Integer threshold) {
    return inventoryRepository.findAll().stream()
        .filter(
            inventory -> (inventory.getQuantity() - inventory.getReservedQuantity()) <= threshold)
        .map(this::mapToResponse)
        .collect(Collectors.toList());
  }

  // ***** END API *****

  // ***** MESSAGE *****

  /**
   * Check inventory for all items in order
   *
   * @param request InventoryCheckEvent
   * @return InventoryCheckResultEvent
   */
  @Override
  @Transactional
  public InventoryCheckResultEvent processInventoryCheck(InventoryCheckEvent request) {
    log.info("Processing inventory check for order: {}", request.getOrderId());

    // Extract product IDs from request
    List<Long> productIds = request.getOrderItems().stream()
        .map(OrderItemDto::getProductId)
        .collect(Collectors.toList());

    // Get all inventory records for the products
    List<Inventory> inventoryList = inventoryRepository.findByProductIdIn(productIds);

    // Create a map for a quick lookup
    Map<Long, Inventory> inventoryMap = new HashMap<>();
    for (Inventory inv : inventoryList) {
      inventoryMap.put(inv.getProductId(), inv);
    }

    // Check if all items are in stock
    List<String> outOfStockItems = new ArrayList<>();

    for (OrderItemDto item : request.getOrderItems()) {
      Inventory inventory = inventoryMap.get(item.getProductId());

      if (inventory == null) {
        outOfStockItems.add("Product ID " + item.getProductId() + " not found in inventory");
        continue;
      }

      if (!inventory.hasAvailableQuantity(item.getQuantity())) {
        outOfStockItems.add("Product ID " + item.getProductId() +
            " has only " + (inventory.getQuantity() - inventory.getReservedQuantity()) +
            " available but " + item.getQuantity() + " requested");
      }
    }

    boolean success = outOfStockItems.isEmpty();
    String message =
        success ? MessageEnum.IN_STOCK.getMessage() :
            MessageEnum.OUT_OF_STOCK.getMessage() + " as: " +
                String.join("; ", outOfStockItems);

    return InventoryCheckResultEvent.builder()
        .orderId(request.getOrderId())
        .available(success)
        .message(message)
        .build();
  }

  /**
   * Process inventory update.
   *
   * @param request InventoryUpdateRequest
   * @return InventoryUpdateResult
   */
  @Override
  @Transactional
  public InventoryUpdateResultEvent processInventoryUpdate(InventoryUpdateEvent request) {
    log.info("Processing inventory update for order: {}, type: {}",
        request.getOrderId(), request.getUpdateType());

    try {
      return switch (request.getUpdateType()) {
        case RESERVE -> reserveInventory(request);
        case COMMIT -> commitInventory(request);
        case RELEASE -> releaseInventory(request);
      };
    } catch (Exception e) {
      log.error("Error processing inventory update for order: {}", request.getOrderId(), e);
      return InventoryUpdateResultEvent.builder()
          .orderId(request.getOrderId())
          .success(false)
          .message("Error processing inventory update : " + e.getMessage())
          .updateType(request.getUpdateType())
          .build();
    }
  }

  /**
   * Reserve inventory (RESERVE) - tạm thời đặt chỗ trước chứ không trừ quantity
   *
   * @param request InventoryUpdateEvent
   * @return InventoryUpdateResultEvent
   */
  private InventoryUpdateResultEvent reserveInventory(InventoryUpdateEvent request) {
    // update for every item
    for (OrderItemDto item : request.getOrderItems()) {
      // lock record to update quantity in stock
      Inventory inventory = inventoryRepository.findWithLockByProductId(item.getProductId())
          .orElseThrow(
              () -> new AppException(MessageEnum.INVENTORY_NOT_EXISTED, item.getProductId()));

      if (!inventory.hasAvailableQuantity(item.getQuantity())) {
        throw new AppException(MessageEnum.NOT_ENOUGH_RESERVE_QUANTITY, item.getProductId());
      }

      inventory.reserveQuantity(item.getQuantity());
      inventoryRepository.save(inventory);
    }

    return InventoryUpdateResultEvent.builder()
        .orderId(request.getOrderId())
        .success(true)
        .message(MessageEnum.RESERVE_INVENTORY_SUCCESS.getMessage())
        .updateType(RESERVE)
        .build();
  }

  /**
   * Commit inventory (COMMIT) - sau khi thanh toán xong mới thực sự trừ đi quantity
   *
   * @param request InventoryUpdateEvent
   * @return InventoryUpdateResultEvent
   */
  private InventoryUpdateResultEvent commitInventory(InventoryUpdateEvent request) {
    // update for every item
    for (OrderItemDto item : request.getOrderItems()) {
      // lock record to update quantity in stock
      Inventory inventory = inventoryRepository.findWithLockByProductId(item.getProductId())
          .orElseThrow(
              () -> new AppException(MessageEnum.INVENTORY_NOT_EXISTED, item.getProductId()));

      inventory.reduceQuantity(item.getQuantity());
      inventory.releaseReservedQuantity(item.getQuantity());
      inventoryRepository.save(inventory);
    }

    return InventoryUpdateResultEvent.builder()
        .orderId(request.getOrderId())
        .success(true)
        .message("Inventory committed successfully")
        .updateType(COMMIT)
        .build();
  }

  /**
   * Release inventory (RELEASE) - xóa bỏ đặt chỗ
   *
   * @param request InventoryUpdateEvent
   * @return InventoryUpdateResultEvent
   */
  private InventoryUpdateResultEvent releaseInventory(InventoryUpdateEvent request) {
    // update for every item
    for (OrderItemDto item : request.getOrderItems()) {
      // lock record to update quantity in stock
      Inventory inventory = inventoryRepository.findWithLockByProductId(item.getProductId())
          .orElseThrow(
              () -> new AppException(MessageEnum.INVENTORY_NOT_EXISTED, item.getProductId()));

      inventory.releaseReservedQuantity(item.getQuantity());
      inventoryRepository.save(inventory);
    }

    return InventoryUpdateResultEvent.builder()
        .orderId(request.getOrderId())
        .success(true)
        .message("Inventory released successfully")
        .updateType(RELEASE)
        .build();
  }

  // ***** END MESSAGE *****

  private InventoryResponse mapToResponse(Inventory inventory) {
    return InventoryResponse.builder()
        .id(inventory.getId())
        .productId(inventory.getProductId())
        .quantity(inventory.getQuantity())
        .reservedQuantity(inventory.getReservedQuantity())
        .availableQuantity(inventory.getQuantity() - inventory.getReservedQuantity())
        .updatedAt(inventory.getUpdatedAt())
        .build();
  }
}
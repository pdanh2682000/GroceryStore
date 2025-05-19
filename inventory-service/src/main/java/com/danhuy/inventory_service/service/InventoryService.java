package com.danhuy.inventory_service.service;

import com.danhuy.common_service.event.inventory.InventoryCheckEvent;
import com.danhuy.common_service.event.inventory.InventoryCheckResultEvent;
import com.danhuy.common_service.event.inventory.InventoryUpdateEvent;
import com.danhuy.common_service.event.inventory.InventoryUpdateResultEvent;
import com.danhuy.inventory_service.dto.InventoryRequest;
import com.danhuy.inventory_service.dto.InventoryResponse;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface InventoryService {

  // ***** API *****
  // Create new inventory record for a product
  InventoryResponse createInventory(InventoryRequest inventoryRequest);

  // Update inventory quantity
  InventoryResponse updateInventory(Long productId, InventoryRequest inventoryRequest);

  // Get inventory by product ID
  InventoryResponse getInventoryByProductId(Long productId);

  // Get all inventory records with pagination
  Page<InventoryResponse> getAllInventory(Pageable pageable);

  // Check if inventory is available for products
  boolean isInStock(Long productId, Integer quantity);

  // Get list of products that are low in stock
  List<InventoryResponse> getLowStockProducts(Integer threshold);
  // ***** END API *****

  // ***** MESSAGE *****
  // Process inventory check request (from Kafka)
  InventoryCheckResultEvent processInventoryCheck(InventoryCheckEvent request);

  // Process inventory update request (from Kafka)
  InventoryUpdateResultEvent processInventoryUpdate(InventoryUpdateEvent request);
  // ***** END MESSAGE *****
}
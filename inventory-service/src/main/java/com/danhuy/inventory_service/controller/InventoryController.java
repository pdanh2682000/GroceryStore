package com.danhuy.inventory_service.controller;

import com.danhuy.common_service.enums.MessageEnum;
import com.danhuy.common_service.response.ApiResponse;
import com.danhuy.common_service.uilts.PagingTransferUtils;
import com.danhuy.inventory_service.dto.InventoryRequest;
import com.danhuy.inventory_service.dto.InventoryResponse;
import com.danhuy.inventory_service.service.InventoryService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

  private final InventoryService inventoryService;

  @PostMapping
  public ResponseEntity<ApiResponse<InventoryResponse>> createInventory(
      @Valid @RequestBody InventoryRequest request) {
    InventoryResponse createdInventory = inventoryService.createInventory(request);

    ApiResponse<InventoryResponse> apiResponse = new ApiResponse<>();
    apiResponse.setCode(MessageEnum.CREATE_INVENTORY_SUCCESS.getCode());
    apiResponse.setMessage(MessageEnum.CREATE_INVENTORY_SUCCESS.getMessage());
    apiResponse.setResult(createdInventory);
    return new ResponseEntity<>(apiResponse, HttpStatus.CREATED);
  }

  @PutMapping("/{productId}")
  public ResponseEntity<ApiResponse<InventoryResponse>> updateInventory(
      @PathVariable Long productId,
      @Valid @RequestBody InventoryRequest request) {
    InventoryResponse updatedInventory = inventoryService.updateInventory(productId, request);

    ApiResponse<InventoryResponse> apiResponse = new ApiResponse<>();
    apiResponse.setCode(MessageEnum.UPDATE_INVENTORY_SUCCESS.getCode());
    apiResponse.setMessage(MessageEnum.UPDATE_INVENTORY_SUCCESS.getMessage());
    apiResponse.setResult(updatedInventory);
    return ResponseEntity.accepted().body(apiResponse);
  }

  @GetMapping("/{productId}")
  public ResponseEntity<ApiResponse<InventoryResponse>> getInventoryByProductId(
      @PathVariable Long productId) {
    InventoryResponse result = inventoryService.getInventoryByProductId(productId);

    ApiResponse<InventoryResponse> apiResponse = new ApiResponse<>();
    apiResponse.setResult(result);
    return ResponseEntity.ok(apiResponse);
  }

  @GetMapping
  public ResponseEntity<ApiResponse<List<InventoryResponse>>> getAllInventory(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size) {
    Pageable pageable = PageRequest.of(page, size);
    Page<InventoryResponse> inventories = inventoryService.getAllInventory(pageable);

    ApiResponse<List<InventoryResponse>> apiResponse = new ApiResponse<>();
    apiResponse.setResult(inventories.getContent());
    // meta data
    apiResponse.setMetadata(PagingTransferUtils.transfersPagingToMetaData(inventories));

    return ResponseEntity.ok(apiResponse);
  }

  @GetMapping("/check")
  public ResponseEntity<Boolean> checkStock(
      @RequestParam Long productId,
      @RequestParam @Min(1) Integer quantity) {
    return ResponseEntity.ok(inventoryService.isInStock(productId, quantity));
  }

  @GetMapping("/low-stock")
  public ResponseEntity<ApiResponse<List<InventoryResponse>>> getLowStockProducts(
      @RequestParam(defaultValue = "5") Integer threshold) {
    ApiResponse<List<InventoryResponse>> apiResponse = new ApiResponse<>();
    apiResponse.setResult(inventoryService.getLowStockProducts(threshold));
    return ResponseEntity.ok(apiResponse);
  }
}
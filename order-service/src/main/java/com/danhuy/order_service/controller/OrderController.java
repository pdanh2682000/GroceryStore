package com.danhuy.order_service.controller;


import com.danhuy.common_service.enums.MessageEnum;
import com.danhuy.common_service.response.ApiResponse;
import com.danhuy.order_service.dto.OrderRequest;
import com.danhuy.order_service.dto.OrderResponse;
import com.danhuy.order_service.service.OrderService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

  private final OrderService orderService;

  @PostMapping
  public ResponseEntity<ApiResponse<OrderResponse>> createOrder(
      @RequestBody OrderRequest orderRequest) {
    OrderResponse orderResponse = orderService.createOrder(orderRequest);

    ApiResponse<OrderResponse> apiResponse = new ApiResponse<>();
    apiResponse.setCode(MessageEnum.CREATE_ORDER_SUCCESS.getCode());
    apiResponse.setMessage(MessageEnum.CREATE_ORDER_SUCCESS.getMessage());
    apiResponse.setResult(orderResponse);

    return new ResponseEntity<>(apiResponse, HttpStatus.CREATED);
  }

  @GetMapping("/{orderId}")
  public ResponseEntity<ApiResponse<OrderResponse>> getOrder(@PathVariable String orderId) {
    OrderResponse orderResponse = orderService.getOrder(orderId);

    ApiResponse<OrderResponse> apiResponse = new ApiResponse<>();
    apiResponse.setResult(orderResponse);

    return ResponseEntity.ok(apiResponse);
  }

  @GetMapping("/user/{userId}")
  public ResponseEntity<ApiResponse<List<OrderResponse>>> getOrdersByUserId(
      @PathVariable String userId) {
    List<OrderResponse> orders = orderService.getOrdersByUserId(userId);

    ApiResponse<List<OrderResponse>> apiResponse = new ApiResponse<>();
    apiResponse.setResult(orders);

    return ResponseEntity.ok(apiResponse);
  }
}

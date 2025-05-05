package com.danhuy.product_service.service;

import com.danhuy.product_service.dto.ProductRequest;
import com.danhuy.product_service.dto.ProductResponse;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductService {

  // Tạo sản phẩm mới
  ProductResponse createProduct(ProductRequest productRequest);

  // Cập nhật sản phẩm
  ProductResponse updateProduct(Long id, ProductRequest productRequest);

  // Xóa sản phẩm (mềm)
  void deleteProduct(Long id);

  // Lấy sản phẩm theo ID
  ProductResponse getProductById(Long id);

  // Lấy tất cả sản phẩm (có phân trang)
  Page<ProductResponse> getAllProducts(Pageable pageable);

  // Tìm kiếm sản phẩm theo tên
  List<ProductResponse> searchProductsByName(String name);

  // Lấy sản phẩm theo danh mục
  List<ProductResponse> getProductsByCategory(Long categoryId);

  // Tìm kiếm sản phẩm nâng cao
  Page<ProductResponse> searchProducts(String name, Long categoryId,
      BigDecimal minPrice, BigDecimal maxPrice,
      Pageable pageable);

}

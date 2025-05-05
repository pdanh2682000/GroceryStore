package com.danhuy.product_service.repository;

import com.danhuy.product_service.entity.Product;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

  // Tìm sản phẩm theo tên (không phân biệt hoa thường)
  List<Product> findByNameContainingIgnoreCase(String name);

  // Tìm sản phẩm theo danh mục
  List<Product> findByCategoryId(Long categoryId);

  // Tìm sản phẩm theo tên và danh mục
  Page<Product> findByNameContainingIgnoreCaseAndCategoryId(String name, Long categoryId,
      Pageable pageable);

  // Tìm sản phẩm theo khoảng giá
  List<Product> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice);

  // Tìm sản phẩm theo tên có phân trang
  Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable);

  // Kiểm tra sản phẩm có tồn tại theo tên
  boolean existsByNameIgnoreCase(String name);

  // Tìm tất cả sản phẩm đang hoạt động
  List<Product> findByIsActiveTrue();

  // Tìm kiếm sản phẩm nâng cao
  @Query("SELECT p FROM Product p WHERE " +
      "(:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
      "(:categoryId IS NULL OR p.category.id = :categoryId) AND " +
      "(:minPrice IS NULL OR p.price >= :minPrice) AND " +
      "(:maxPrice IS NULL OR p.price <= :maxPrice) AND " +
      "p.isActive = true")
  Page<Product> searchProducts(
      @Param("name") String name,
      @Param("categoryId") Long categoryId,
      @Param("minPrice") BigDecimal minPrice,
      @Param("maxPrice") BigDecimal maxPrice,
      Pageable pageable);

}

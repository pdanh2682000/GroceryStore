package com.danhuy.order_service.repository;

import com.danhuy.order_service.entity.Order;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, String> {

  List<Order> findByUserId(String userId);
}

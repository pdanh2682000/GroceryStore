package com.danhuy.inventory_service.repository;

import com.danhuy.inventory_service.entity.Inventory;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {

  Optional<Inventory> findByProductId(Long productId);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  Optional<Inventory> findWithLockByProductId(Long productId);

  List<Inventory> findByProductIdIn(List<Long> productIds);
}
package com.danhuy.inventory_service.entity;

import com.danhuy.common_service.enums.MessageEnum;
import com.danhuy.common_service.exception.ex.AppException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "inventory")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Inventory {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Long productId;

  /**
   * root of quantity.
   */
  @Column(nullable = false)
  private Integer quantity;

  /**
   * current of quantity.
   */
  @Column(nullable = false)
  private Integer reservedQuantity;

  @Version
  private Long version;

  @CreationTimestamp // auto setup timestamp
  private LocalDateTime createdAt;

  @UpdateTimestamp // auto setup timestamp
  private LocalDateTime updatedAt;

  /**
   * check available quantity
   *
   * @param requestedQuantity int
   * @return true if available
   */
  public boolean hasAvailableQuantity(int requestedQuantity) {
    return (quantity - reservedQuantity) >= requestedQuantity;
  }

  /**
   * reserve amount of product. (ordered)
   *
   * @param amount int
   */
  public void reserveQuantity(int amount) {
    if (!hasAvailableQuantity(amount)) {
      throw new AppException(MessageEnum.NOT_ENOUGH_RESERVE_QUANTITY);
    }
    this.reservedQuantity += amount;
  }

  /**
   * release amount of product. (refund)
   *
   * @param amount int
   */
  public void releaseReservedQuantity(int amount) {
    if (this.reservedQuantity < amount) {
      throw new AppException(MessageEnum.NOT_RELEASE_RESERVE_QUANTITY);
    }
    this.reservedQuantity -= amount;
  }

  /**
   * reduce root of quantity.
   *
   * @param amount int
   */
  public void reduceQuantity(int amount) {
    if (this.quantity < amount) {
      throw new AppException(MessageEnum.NOT_REDUCE_QUANTITY);
    }
    this.quantity -= amount;
    this.reservedQuantity = Math.min(this.reservedQuantity, this.quantity);
  }

  /**
   * increase root of quantity.
   *
   * @param amount int
   */
  public void addQuantity(int amount) {
    this.quantity += amount;
  }
}
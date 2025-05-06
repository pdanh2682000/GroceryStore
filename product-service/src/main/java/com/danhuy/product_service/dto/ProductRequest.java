package com.danhuy.product_service.dto;

import com.danhuy.common_service.validate.EnumValue;
import com.danhuy.common_service.validate.PhoneNumberFormat;
import com.danhuy.product_service.const_enum.ModelType;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequest {

  @NotBlank(message = "Product name is required")
  private String name;

  private String description;

  @NotNull(message = "Price is required")
  @Positive(message = "Price must be positive")
  private BigDecimal price;

  private Long categoryId;

  private String imageUrl;

  private Integer stock;

  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy")
  private LocalDate expiry;

  @PhoneNumberFormat(message = "Phone number must be valid")
  private String phoneSupplier;

  @EnumValue(name = "modelType", enumClass = ModelType.class)
  private String modelType;
}

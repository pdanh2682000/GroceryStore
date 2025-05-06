package com.danhuy.product_service.const_enum;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum ModelType {
  @JsonProperty("new")
  NEW,
  @JsonProperty("used")
  USED
}

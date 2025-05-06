package com.danhuy.common_service.uilts;

import java.util.HashMap;
import java.util.Map;
import org.springframework.data.domain.Page;

public class PagingTransferUtils {

  public static <T> Map<String, Object> transfersPagingToMetaData(Page<T> pages) {
    Map<String, Object> result = new HashMap<>();
    result.put("totalPages", pages.getTotalPages());
    result.put("totalElements", pages.getTotalElements());
    result.put("number", pages.getNumber());
    result.put("size", pages.getSize());
    return result;
  }

}

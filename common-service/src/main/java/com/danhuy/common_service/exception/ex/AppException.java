package com.danhuy.common_service.exception.ex;

import com.danhuy.common_service.const_enum.MessageEnum;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class AppException extends RuntimeException {

  private MessageEnum messageEnum;

  public AppException(MessageEnum messageEnum) {
    super(messageEnum.getMessage());
    this.messageEnum = messageEnum;
  }

}

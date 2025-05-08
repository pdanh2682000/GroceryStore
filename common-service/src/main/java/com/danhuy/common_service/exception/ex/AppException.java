package com.danhuy.common_service.exception.ex;

import com.danhuy.common_service.const_enum.MessageEnum;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class AppException extends RuntimeException {

  private MessageEnum messageEnum;
  private Object[] argsFormated;

  public AppException(MessageEnum messageEnum) {
    super(messageEnum.getMessage());
    this.messageEnum = messageEnum;
  }

  public AppException(MessageEnum messageEnum, Object... args) {
    super(messageEnum.getMessage());
    this.messageEnum = messageEnum;
    this.argsFormated = args;
  }

}

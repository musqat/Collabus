package com.muscat.Collabus.common.exception;

import com.muscat.Collabus.enums.response.BaseResponseEnum;
import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {

  private final BaseResponseEnum response;

  public BusinessException(BaseResponseEnum response) {
    super(response.getMessage());
    this.response = response;
  }

  public BaseResponseEnum getResponse() {
    return response;
  }
}

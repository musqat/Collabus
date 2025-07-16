package com.muscat.Collabus.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException{

  public ResourceNotFoundException(String resourceName, String fieldName, String fieldValue) {
    super(String.format("입력한 %s 값 '%s'에 해당하는 %s을(를) 찾을 수 없습니다.", fieldName, fieldValue, resourceName));
  }


}

package com.muscat.Collabus.common.exception;

import com.muscat.Collabus.enums.response.BaseResponseEnum;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@Getter
@ResponseStatus(HttpStatus.CONFLICT)
public class ResourceAlreadyExistsException extends RuntimeException {

  private final String code;

  public ResourceAlreadyExistsException(BaseResponseEnum response) {
    super(response.getMessage());
    this.code = response.getCode();
  }

  public ResourceAlreadyExistsException(String message) {
    super(message);
    this.code = HttpStatus.CONFLICT.toString();
  }
}

package com.muscat.Collabus.User.exception;

import com.muscat.Collabus.enums.UserResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class UserAlreadyExistsException extends RuntimeException {
  private final String code;

  public UserAlreadyExistsException(UserResponse response) {
    super(response.getMessage());
    this.code = response.getCode();
  }

  public String getCode() {
    return code;
  }
}

package com.muscat.Collabus.User.exception;

import com.muscat.Collabus.common.dto.ErrorResponseDto;
import java.time.LocalDateTime;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

@RestControllerAdvice(basePackages = "com.muscat.Collabus.User")
public class UserExceptionHandler {

  @ExceptionHandler(UserAlreadyExistsException.class)
  public ResponseEntity<ErrorResponseDto> handleUserAlreadyExistsException(
      UserAlreadyExistsException ex, WebRequest request) {
    ErrorResponseDto dto = new ErrorResponseDto(
        request.getDescription(false),
        HttpStatus.BAD_REQUEST,
        ex.getMessage(),
        LocalDateTime.now()
    );
    return new ResponseEntity<>(dto, HttpStatus.BAD_REQUEST);
  }
}

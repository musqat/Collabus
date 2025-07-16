package com.muscat.Collabus.common.exception;

import com.muscat.Collabus.common.dto.ErrorResponseDto;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

  @Override
  protected ResponseEntity<Object> handleMethodArgumentNotValid(
      MethodArgumentNotValidException ex,
      HttpHeaders headers,
      HttpStatusCode status,
      WebRequest request
  ) {
    Map<String, String> errors = new HashMap<>();
    for (ObjectError error : ex.getBindingResult().getAllErrors()) {
      String fieldName = ((FieldError) error).getField();
      String message = error.getDefaultMessage();
      errors.put(fieldName, message);
    }

    ErrorResponseDto dto = new ErrorResponseDto(
        request.getDescription(false),
        HttpStatus.BAD_REQUEST,
        "인증 실패",
        LocalDateTime.now()
    );

    return new ResponseEntity<>(dto, HttpStatus.BAD_REQUEST);
  }

  // 기본 예외
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponseDto> handleGeneralException(
      Exception ex, WebRequest request
  ) {
    ErrorResponseDto dto = new ErrorResponseDto(
        request.getDescription(false),
        HttpStatus.INTERNAL_SERVER_ERROR,
        ex.getMessage(),
        LocalDateTime.now()
    );
    return new ResponseEntity<>(dto, HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<ErrorResponseDto> ResourceNotFoundException(
      ResourceNotFoundException exception, WebRequest webRequest) {
    ErrorResponseDto errorResponseDto = new ErrorResponseDto(
        webRequest.getDescription(false),
        HttpStatus.NOT_FOUND,
        exception.getMessage(),
        LocalDateTime.now()
    );
    return new ResponseEntity<>(errorResponseDto, HttpStatus.NOT_FOUND);
  }

}

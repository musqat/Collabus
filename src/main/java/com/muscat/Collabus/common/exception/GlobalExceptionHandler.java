package com.muscat.Collabus.common.exception;

import com.muscat.Collabus.common.dto.ErrorResponseDto;
import com.muscat.Collabus.enums.response.ErrorType;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<ErrorResponseDto> handleNotFound(ResourceNotFoundException ex,
      HttpServletRequest request) {
    return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage(), ErrorType.NOT_FOUND, request);
  }

  @ExceptionHandler(ResourceAlreadyExistsException.class)
  public ResponseEntity<ErrorResponseDto> handleResourceExists(ResourceAlreadyExistsException ex,
      HttpServletRequest request) {
    return buildErrorResponse(HttpStatus.CONFLICT, ex.getMessage(), ErrorType.CONFLICT, request);
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<ErrorResponseDto> handleAccessDenied(AccessDeniedException ex,
      HttpServletRequest request) {
    return buildErrorResponse(HttpStatus.FORBIDDEN, ex.getMessage(), ErrorType.FORBIDDEN, request);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponseDto> handleValidation(MethodArgumentNotValidException ex,
      HttpServletRequest request) {
    String errorMsg = ex.getBindingResult().getFieldErrors().stream()
        .map(err -> err.getField() + ": " + err.getDefaultMessage())
        .findFirst()
        .orElse("입력값이 유효하지 않습니다.");
    return buildErrorResponse(HttpStatus.BAD_REQUEST, errorMsg, ErrorType.VALIDATION, request);
  }

  //예상된 실패
  @ExceptionHandler(BusinessException.class)
  public ResponseEntity<ErrorResponseDto> handleBusiness(BusinessException ex,
      HttpServletRequest request) {
    return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), ErrorType.BUSINESS, request);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponseDto> handleGeneral(Exception ex,
      HttpServletRequest request) {
    return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), ErrorType.SYSTEM,
        request);
  }

  private ResponseEntity<ErrorResponseDto> buildErrorResponse(
      HttpStatus status, String message, ErrorType errorType, HttpServletRequest request) {
    ErrorResponseDto error = new ErrorResponseDto(
        request.getRequestURI(), status, message, LocalDateTime.now(), errorType);
    return ResponseEntity.status(status).body(error);
  }
}

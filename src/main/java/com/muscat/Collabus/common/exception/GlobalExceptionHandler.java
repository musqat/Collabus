package com.muscat.Collabus.common.exception;

import com.muscat.Collabus.common.dto.ErrorResponseDto;
import com.muscat.Collabus.enums.response.ErrorType;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  private static final String GENERIC_ERROR_MESSAGE = "서버 오류가 발생했습니다.";

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

  // 잘못된 쿼리 파라미터는  400
  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ErrorResponseDto> handleIllegalArgument(IllegalArgumentException ex,
      HttpServletRequest request) {
    return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), ErrorType.VALIDATION,
        request);
  }

  // 잘못된 형식의 요청 본문은 클라이언트 오류
  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ErrorResponseDto> handleNotReadable(HttpMessageNotReadableException ex,
      HttpServletRequest request) {
    return buildErrorResponse(HttpStatus.BAD_REQUEST, "요청 본문을 해석할 수 없습니다.",
        ErrorType.VALIDATION, request);
  }

  // 매핑되지 않은 경로는 정적 리소스 조회로 넘어가 예외가 되므로 404 로 변환
  @ExceptionHandler(NoResourceFoundException.class)
  public ResponseEntity<ErrorResponseDto> handleNoResource(NoResourceFoundException ex,
      HttpServletRequest request) {
    return buildErrorResponse(HttpStatus.NOT_FOUND, "요청한 경로를 찾을 수 없습니다.",
        ErrorType.NOT_FOUND, request);
  }

  @ExceptionHandler(MaxUploadSizeExceededException.class)
  public ResponseEntity<ErrorResponseDto> handleMaxUploadSize(MaxUploadSizeExceededException ex,
      HttpServletRequest request) {
    return buildErrorResponse(HttpStatus.PAYLOAD_TOO_LARGE, "업로드 가능한 파일 크기를 초과했습니다.",
        ErrorType.VALIDATION, request);
  }

  //예상된 실패
  @ExceptionHandler(BusinessException.class)
  public ResponseEntity<ErrorResponseDto> handleBusiness(BusinessException ex,
      HttpServletRequest request) {
    return buildErrorResponse(resolveStatus(ex), ex.getMessage(), ErrorType.BUSINESS, request);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponseDto> handleGeneral(Exception ex,
      HttpServletRequest request) {
    // 예외 메시지에는 내부 구조가 드러날 수 있으므로 로그로만 남기고 클라이언트에는 고정 문구를 반환한다
    log.error("처리되지 않은 예외 - {} {}", request.getMethod(), request.getRequestURI(), ex);
    return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, GENERIC_ERROR_MESSAGE,
        ErrorType.SYSTEM, request);
  }

  // 응답 enum 이 들고 있는 코드를 HTTP 상태로 사용한다. 권한 오류가 400 으로 뭉개지는 것을 막는다.
  private HttpStatus resolveStatus(BusinessException ex) {
    if (ex.getResponse() == null) {
      return HttpStatus.BAD_REQUEST;
    }
    try {
      HttpStatus status = HttpStatus.resolve(Integer.parseInt(ex.getResponse().getCode()));
      // 응답 enum 에는 표준 HTTP 코드가 아닌 값(420, 421, 423 등)도 섞여 있으므로 오류 코드만 사용한다
      return status != null && status.isError() ? status : HttpStatus.BAD_REQUEST;
    } catch (NumberFormatException e) {
      return HttpStatus.BAD_REQUEST;
    }
  }

  private ResponseEntity<ErrorResponseDto> buildErrorResponse(
      HttpStatus status, String message, ErrorType errorType, HttpServletRequest request) {
    ErrorResponseDto error = new ErrorResponseDto(
        request.getRequestURI(), status, message, LocalDateTime.now(), errorType);
    return ResponseEntity.status(status).body(error);
  }
}

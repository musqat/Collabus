package com.muscat.Collabus.config.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.muscat.Collabus.common.dto.ErrorResponseDto;
import com.muscat.Collabus.enums.response.ErrorType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

/**
 * 시큐리티 필터 단계에서 발생한 인증·인가 실패를 컨트롤러 예외와 같은 형식으로 내려준다.
 * 이 단계는 @RestControllerAdvice 가 잡지 못해 따로 처리해야 한다.
 */
@Component
@RequiredArgsConstructor
public class SecurityErrorResponder {

  private final ObjectMapper objectMapper;

  public void write(HttpServletRequest request, HttpServletResponse response,
      HttpStatus status, String message, ErrorType errorType) throws IOException {
    response.setStatus(status.value());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setCharacterEncoding(StandardCharsets.UTF_8.name());

    ErrorResponseDto body = new ErrorResponseDto(
        request.getRequestURI(), status, message, LocalDateTime.now(), errorType);
    objectMapper.writeValue(response.getWriter(), body);
  }
}

package com.muscat.Collabus.config.security;

import com.muscat.Collabus.enums.response.ErrorType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

/**
 * 인증 자체가 없는 요청에 대한 응답.
 * 기본 구현은 401 이 아니라 403 을 반환한다. 클라이언트 인터셉터가 401 에만 토큰 재발급을
 * 시도하므로, 403 이면 만료된 세션을 되살릴 기회 없이 실패한다.
 */
@Component
@RequiredArgsConstructor
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

  private final SecurityErrorResponder responder;

  @Override
  public void commence(HttpServletRequest request, HttpServletResponse response,
      AuthenticationException authException) throws IOException {
    responder.write(request, response, HttpStatus.UNAUTHORIZED,
        "인증이 필요합니다.", ErrorType.UNAUTHORIZED);
  }
}

package com.muscat.Collabus.config.security;

import com.muscat.Collabus.enums.response.ErrorType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

/**
 * 인증은 됐지만 권한이 모자란 요청에 대한 응답. 이 경우는 재발급으로 해결되지 않으므로 403
 */
@Component
@RequiredArgsConstructor
public class RestAccessDeniedHandler implements AccessDeniedHandler {

  private final SecurityErrorResponder responder;

  @Override
  public void handle(HttpServletRequest request, HttpServletResponse response,
      AccessDeniedException accessDeniedException) throws IOException {
    responder.write(request, response, HttpStatus.FORBIDDEN,
        "권한이 없습니다.", ErrorType.FORBIDDEN);
  }
}

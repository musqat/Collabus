package com.muscat.Collabus.config.ratelimit;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 요청을 보낸 클라이언트의 IP 를 정한다. 앞단 프록시가 덮어쓰는 헤더 하나만 믿는다.
 */
@Component
public class ClientIpResolver {

  private final String trustedHeader;

  public ClientIpResolver(@Value("${rate-limit.client-ip-header:}") String trustedHeader) {
    this.trustedHeader = trustedHeader;
  }

  // 설정한 헤더 값, 없으면 접속 주소
  public String resolve(HttpServletRequest request) {
    if (StringUtils.hasText(trustedHeader)) {
      String value = request.getHeader(trustedHeader);
      if (StringUtils.hasText(value)) {
        return value.trim();
      }
    }
    return request.getRemoteAddr();
  }
}

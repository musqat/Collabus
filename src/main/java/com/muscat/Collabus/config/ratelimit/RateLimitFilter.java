package com.muscat.Collabus.config.ratelimit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.muscat.Collabus.common.dto.ResponseDto;
import com.muscat.Collabus.config.ratelimit.RateLimitPolicy.Rule;
import com.muscat.Collabus.enums.response.CommonResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * 로그인 · 회원가입 요청을 IP 단위로 세어 한도를 넘으면 429 로 막는다.
 */
@Slf4j
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

  private static final String KEY_PREFIX = "RL:";

  private final RateLimitPolicy policy;
  private final ClientIpResolver clientIpResolver;
  private final RedisWindowCounter windowCounter;
  private final ObjectMapper objectMapper;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain chain) throws ServletException, IOException {
    Rule rule = policy.ruleFor(request).orElse(null);
    if (rule != null) {
      String key = KEY_PREFIX + rule.name() + ":" + clientIpResolver.resolve(request);
      try {
        if (windowCounter.increment(key, rule.windowMillis()) > rule.limit()) {
          reject(response, windowCounter.remainingMillis(key));
          return;
        }
      } catch (DataAccessException e) {
        // Redis 장애 시 제한 없이 통과
        log.warn("요청 제한 카운터 사용 불가 - {} 요청 통과", rule.name(), e);
      }
    }
    chain.doFilter(request, response);
  }

  // 429 와 남은 초
  private void reject(HttpServletResponse response, long remainingMillis) throws IOException {
    long retryAfterSeconds = Math.max(1, (remainingMillis + 999) / 1000);
    response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
    response.setHeader(HttpHeaders.RETRY_AFTER, String.valueOf(retryAfterSeconds));
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setCharacterEncoding(StandardCharsets.UTF_8.name());
    objectMapper.writeValue(response.getWriter(),
        new ResponseDto(CommonResponse.TOO_MANY_REQUESTS));
  }
}

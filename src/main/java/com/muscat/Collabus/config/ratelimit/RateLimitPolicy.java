package com.muscat.Collabus.config.ratelimit;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

/**
 * IP 단위로 횟수를 제한할 경로와 한도를 정한다.
 */
@Component
public class RateLimitPolicy {

  private static final long MINUTE_MILLIS = 60_000L;
  private static final long HOUR_MILLIS = 60 * MINUTE_MILLIS;

  private final boolean enabled;
  private final Map<String, Rule> rules;

  public RateLimitPolicy(
      @Value("${rate-limit.enabled:true}") boolean enabled,
      @Value("${rate-limit.login-per-minute:20}") int loginPerMinute,
      @Value("${rate-limit.register-per-hour:5}") int registerPerHour) {
    this.enabled = enabled;
    this.rules = Map.of(
        "/api/users/login", new Rule("login", loginPerMinute, MINUTE_MILLIS),
        "/api/users/register", new Rule("register", registerPerHour, HOUR_MILLIS));
  }

  // 요청에 걸리는 규칙, 꺼져 있거나 대상이 아니면 없음
  public Optional<Rule> ruleFor(HttpServletRequest request) {
    if (!enabled || !HttpMethod.POST.matches(request.getMethod())) {
      return Optional.empty();
    }
    return Optional.ofNullable(rules.get(request.getRequestURI()));
  }

  public record Rule(String name, int limit, long windowMillis) {

  }
}

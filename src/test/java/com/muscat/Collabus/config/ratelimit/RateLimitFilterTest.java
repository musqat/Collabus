package com.muscat.Collabus.config.ratelimit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

@DisplayName("IP 단위 요청 제한 필터")
class RateLimitFilterTest {

  private static final String IP = "203.0.113.7";
  private static final String LOGIN_KEY = "RL:login:" + IP;

  private RedisWindowCounter windowCounter;
  private ClientIpResolver clientIpResolver;
  private MockHttpServletResponse response;
  private FilterChain chain;

  @BeforeEach
  void setUp() {
    windowCounter = mock(RedisWindowCounter.class);
    clientIpResolver = mock(ClientIpResolver.class);
    response = new MockHttpServletResponse();
    chain = mock(FilterChain.class);
  }

  private RateLimitFilter filter(boolean enabled) {
    return new RateLimitFilter(new RateLimitPolicy(enabled, 2, 1), clientIpResolver,
        windowCounter, new ObjectMapper());
  }

  private MockHttpServletRequest post(String uri) {
    MockHttpServletRequest request = new MockHttpServletRequest("POST", uri);
    when(clientIpResolver.resolve(request)).thenReturn(IP);
    return request;
  }

  @Test
  @DisplayName("한도까지는 IP 별로 세고 통과시킨다")
  void passesWithinLimit() throws Exception {
    MockHttpServletRequest request = post("/api/users/login");
    when(windowCounter.increment(LOGIN_KEY, 60_000L)).thenReturn(2L);

    filter(true).doFilter(request, response, chain);

    verify(chain).doFilter(request, response);
    assertThat(response.getStatus()).isEqualTo(200);
  }

  @Test
  @DisplayName("한도를 넘으면 429 와 남은 초를 주고 다음으로 넘기지 않는다")
  void rejectsOverLimit() throws Exception {
    MockHttpServletRequest request = post("/api/users/login");
    when(windowCounter.increment(LOGIN_KEY, 60_000L)).thenReturn(3L);
    when(windowCounter.remainingMillis(LOGIN_KEY)).thenReturn(41_200L);

    filter(true).doFilter(request, response, chain);

    verify(chain, never()).doFilter(request, response);
    assertThat(response.getStatus()).isEqualTo(429);
    assertThat(response.getHeader("Retry-After")).isEqualTo("42");
    assertThat(response.getContentAsString()).contains("\"statusMsg\"").contains("429");
  }

  @Test
  @DisplayName("회원가입은 1시간 창으로 따로 센다")
  void registerUsesHourWindow() throws Exception {
    MockHttpServletRequest request = post("/api/users/register");
    when(windowCounter.increment("RL:register:" + IP, 3_600_000L)).thenReturn(2L);
    when(windowCounter.remainingMillis("RL:register:" + IP)).thenReturn(1_000L);

    filter(true).doFilter(request, response, chain);

    assertThat(response.getStatus()).isEqualTo(429);
  }

  @Test
  @DisplayName("대상이 아닌 요청은 세지 않는다")
  void ignoresOtherRequests() throws Exception {
    MockHttpServletRequest other = new MockHttpServletRequest("POST", "/api/workspaces");
    MockHttpServletRequest getLogin = new MockHttpServletRequest("GET", "/api/users/login");

    filter(true).doFilter(other, response, chain);
    filter(true).doFilter(getLogin, new MockHttpServletResponse(), new MockFilterChain());

    verify(windowCounter, never()).increment(anyString(), anyLong());
    verify(chain).doFilter(other, response);
  }

  @Test
  @DisplayName("Redis 를 쓸 수 없으면 제한 없이 통과시킨다")
  void passesWhenRedisDown() throws Exception {
    MockHttpServletRequest request = post("/api/users/login");
    when(windowCounter.increment(LOGIN_KEY, 60_000L))
        .thenThrow(new RedisConnectionFailureException("연결 실패"));

    filter(true).doFilter(request, response, chain);

    verify(chain).doFilter(request, response);
  }

  @Test
  @DisplayName("꺼져 있으면 세지 않는다")
  void skipsWhenDisabled() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/users/login");

    filter(false).doFilter(request, response, chain);

    verify(windowCounter, never()).increment(anyString(), anyLong());
    verify(chain).doFilter(request, response);
  }
}

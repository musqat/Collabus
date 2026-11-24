package com.muscat.Collabus.config.ratelimit;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

@DisplayName("클라이언트 IP 판별")
class ClientIpResolverTest {

  private static final String PROXY_ADDR = "10.0.0.5";

  private MockHttpServletRequest requestFromProxy() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setRemoteAddr(PROXY_ADDR);
    return request;
  }

  @Test
  @DisplayName("설정한 헤더에 값이 있으면 그 값을 쓴다")
  void usesTrustedHeader() {
    MockHttpServletRequest request = requestFromProxy();
    request.addHeader("Fly-Client-IP", " 203.0.113.7 ");

    assertThat(new ClientIpResolver("Fly-Client-IP").resolve(request)).isEqualTo("203.0.113.7");
  }

  @Test
  @DisplayName("설정한 헤더가 비어 있으면 접속 주소를 쓴다")
  void fallsBackWhenHeaderMissing() {
    assertThat(new ClientIpResolver("Fly-Client-IP").resolve(requestFromProxy()))
        .isEqualTo(PROXY_ADDR);
  }

  @Test
  @DisplayName("헤더를 설정하지 않으면 접속 주소를 쓴다")
  void usesRemoteAddrWithoutHeader() {
    assertThat(new ClientIpResolver("").resolve(requestFromProxy())).isEqualTo(PROXY_ADDR);
  }

  @Test
  @DisplayName("클라이언트가 써 보낸 X-Forwarded-For 는 무시한다")
  void ignoresForgedForwardedFor() {
    MockHttpServletRequest request = requestFromProxy();
    request.addHeader("X-Forwarded-For", "1.2.3.4");
    request.addHeader("X-Real-IP", "198.51.100.20");

    assertThat(new ClientIpResolver("X-Real-IP").resolve(request)).isEqualTo("198.51.100.20");
    assertThat(new ClientIpResolver("").resolve(request)).isEqualTo(PROXY_ADDR);
  }
}

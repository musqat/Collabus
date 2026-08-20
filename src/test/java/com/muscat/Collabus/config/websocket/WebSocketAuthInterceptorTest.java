package com.muscat.Collabus.config.websocket;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.muscat.Collabus.config.jwt.JwtUtil;
import com.muscat.Collabus.config.security.CustomUserDetails;
import com.muscat.Collabus.config.token.RefreshTokenService;

import io.jsonwebtoken.Claims;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.core.Authentication;

/**
 * STOMP CONNECT 시점의 인증 경계를 확인한다.
 * 여기서 막지 못하면 토큰 없이 실시간 채널에 붙는다.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("WebSocket 인증")
class WebSocketAuthInterceptorTest {

  private static final String TOKEN = "valid-token";

  @Mock
  private JwtUtil jwtUtil;

  @Mock
  private RefreshTokenService refreshTokenService;

  @Mock
  private MessageChannel channel;

  @Mock
  private Claims claims;

  @InjectMocks
  private WebSocketAuthInterceptor interceptor;

  @BeforeEach
  void setUp() {
    when(refreshTokenService.isBlacklisted(anyString())).thenReturn(false);
    when(jwtUtil.validateToken(anyString())).thenReturn(true);
    when(jwtUtil.parseClaims(anyString())).thenReturn(claims);
    when(claims.get("userId", Number.class)).thenReturn(1L);
    when(claims.getSubject()).thenReturn("user@test.com");
    when(claims.get("role", String.class)).thenReturn("USER");
    when(claims.get("displayName", String.class)).thenReturn("user#1234");
  }

  /** CONNECT 프레임을 만든다. authHeader 가 null 이면 헤더를 붙이지 않는다 */
  private Message<byte[]> connectMessage(String authHeader) {
    StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
    if (authHeader != null) {
      accessor.setNativeHeader("Authorization", authHeader);
    }
    // 인터셉터가 인증 주체를 넣으므로 헤더를 잠그지 않는다
    accessor.setLeaveMutable(true);
    return MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
  }

  private Message<byte[]> frame(StompCommand command) {
    StompHeaderAccessor accessor = StompHeaderAccessor.create(command);
    return MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
  }

  @Test
  @DisplayName("CONNECT 가 아닌 프레임은 그대로 넘긴다")
  void nonConnectFrame_PassesThrough() {
    Message<byte[]> message = frame(StompCommand.SEND);

    assertThat(interceptor.preSend(message, channel)).isSameAs(message);

    verify(jwtUtil, never()).validateToken(anyString());
  }

  @Test
  @DisplayName("STOMP 프레임이 아니면 그대로 넘긴다")
  void nonStompMessage_PassesThrough() {
    Message<byte[]> message = MessageBuilder.withPayload(new byte[0]).build();

    assertThat(interceptor.preSend(message, channel)).isSameAs(message);

    verify(jwtUtil, never()).validateToken(anyString());
  }

  @Test
  @DisplayName("Authorization 헤더가 없으면 연결을 막는다")
  void missingHeader_Rejected() {
    assertThatThrownBy(() -> interceptor.preSend(connectMessage(null), channel))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Authorization");
  }

  @Test
  @DisplayName("Bearer 형식이 아니면 연결을 막는다")
  void wrongScheme_Rejected() {
    assertThatThrownBy(() -> interceptor.preSend(connectMessage(TOKEN), channel))
        .isInstanceOf(IllegalArgumentException.class);

    verify(jwtUtil, never()).validateToken(anyString());
  }

  @Test
  @DisplayName("블랙리스트에 오른 토큰은 연결을 막는다")
  void blacklistedToken_Rejected() {
    when(refreshTokenService.isBlacklisted(TOKEN)).thenReturn(true);

    assertThatThrownBy(() -> interceptor.preSend(connectMessage("Bearer " + TOKEN), channel))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("만료");

    verify(jwtUtil, never()).validateToken(anyString());
  }

  @Test
  @DisplayName("검증에 실패한 토큰은 연결을 막는다")
  void invalidToken_Rejected() {
    when(jwtUtil.validateToken(TOKEN)).thenReturn(false);

    assertThatThrownBy(() -> interceptor.preSend(connectMessage("Bearer " + TOKEN), channel))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("유효하지 않은");
  }

  @Test
  @DisplayName("유효한 토큰이면 인증 주체를 붙여 통과시킨다")
  void validToken_SetsPrincipal() {
    Message<byte[]> message = connectMessage("Bearer " + TOKEN);

    Message<?> result = interceptor.preSend(message, channel);

    StompHeaderAccessor accessor =
        StompHeaderAccessor.wrap(result);
    Authentication authentication = (Authentication) accessor.getUser();
    assertThat(authentication).isNotNull();

    CustomUserDetails principal = (CustomUserDetails) authentication.getPrincipal();
    assertThat(principal.getUserId()).isEqualTo(1L);
    assertThat(principal.getUsername()).isEqualTo("user@test.com");
  }

  @Test
  @DisplayName("유효한 토큰이면 예외를 내지 않는다")
  void validToken_NoException() {
    assertThatCode(() -> interceptor.preSend(connectMessage("Bearer " + TOKEN), channel))
        .doesNotThrowAnyException();
  }
}

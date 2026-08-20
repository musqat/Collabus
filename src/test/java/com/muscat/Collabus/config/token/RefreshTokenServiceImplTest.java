package com.muscat.Collabus.config.token;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("리프레시 토큰")
class RefreshTokenServiceImplTest {

  private static final String EMAIL = "a@b.com";
  private static final String TOKEN = "token-value";

  @Mock
  private StringRedisTemplate redisTemplate;

  @Mock
  private ValueOperations<String, String> valueOperations;

  @InjectMocks
  private RefreshTokenServiceImpl refreshTokenService;

  @BeforeEach
  void setUp() {
    when(redisTemplate.opsForValue()).thenReturn(valueOperations);
  }

  @Test
  @DisplayName("토큰을 TTL 과 함께 저장한다")
  void saveRefreshToken() {
    refreshTokenService.saveRefreshToken(EMAIL, TOKEN, 1000L);

    verify(valueOperations, times(1))
        .set("RT:" + EMAIL, TOKEN, 1000L, TimeUnit.MILLISECONDS);
  }

  @Test
  @DisplayName("저장된 토큰을 돌려준다")
  void getRefreshToken_Present() {
    when(valueOperations.get("RT:" + EMAIL)).thenReturn(TOKEN);

    assertThat(refreshTokenService.getRefreshToken(EMAIL)).contains(TOKEN);
  }

  @Test
  @DisplayName("저장된 토큰이 없으면 비어 있다")
  void getRefreshToken_Empty() {
    when(valueOperations.get("RT:" + EMAIL)).thenReturn(null);

    assertThat(refreshTokenService.getRefreshToken(EMAIL)).isEmpty();
  }

  @Test
  @DisplayName("토큰을 지운다")
  void deleteRefreshToken() {
    refreshTokenService.deleteRefreshToken(EMAIL);

    verify(redisTemplate, times(1)).delete("RT:" + EMAIL);
  }

  @Test
  @DisplayName("블랙리스트에 만료 시각까지 담는다")
  void blacklistAccessToken() {
    refreshTokenService.blacklistAccessToken(TOKEN, 500L);

    verify(valueOperations, times(1))
        .set("BL:" + TOKEN, "logout", 500L, TimeUnit.MILLISECONDS);
  }

  @Test
  @DisplayName("블랙리스트에 있으면 참이다")
  void isBlacklisted_True() {
    when(redisTemplate.hasKey("BL:" + TOKEN)).thenReturn(true);

    assertThat(refreshTokenService.isBlacklisted(TOKEN)).isTrue();
  }

  @Test
  @DisplayName("hasKey 가 null 이면 거짓이다")
  void isBlacklisted_Null() {
    when(redisTemplate.hasKey("BL:" + TOKEN)).thenReturn(null);

    assertThat(refreshTokenService.isBlacklisted(TOKEN)).isFalse();
  }

  @Test
  @DisplayName("첫 실패에는 TTL 을 건다")
  void incrementLoginFailure_First() {
    when(valueOperations.increment("LF:" + EMAIL)).thenReturn(1L);

    assertThat(refreshTokenService.incrementLoginFailure(EMAIL)).isEqualTo(1);

    verify(redisTemplate, times(1))
        .expire("LF:" + EMAIL, 10 * 60 * 1000L, TimeUnit.MILLISECONDS);
  }

  @Test
  @DisplayName("두 번째 실패부터는 TTL 을 다시 걸지 않는다")
  void incrementLoginFailure_Second() {
    when(valueOperations.increment("LF:" + EMAIL)).thenReturn(2L);

    assertThat(refreshTokenService.incrementLoginFailure(EMAIL)).isEqualTo(2);

    verify(redisTemplate, never()).expire(anyString(), anyLong(), any(TimeUnit.class));
  }

  @Test
  @DisplayName("증가 결과가 null 이면 1 로 본다")
  void incrementLoginFailure_Null() {
    when(valueOperations.increment("LF:" + EMAIL)).thenReturn(null);

    assertThat(refreshTokenService.incrementLoginFailure(EMAIL)).isEqualTo(1);
  }

  @Test
  @DisplayName("실패 기록을 지운다")
  void resetLoginFailure() {
    refreshTokenService.resetLoginFailure(EMAIL);

    verify(redisTemplate, times(1)).delete("LF:" + EMAIL);
  }

  @Test
  @DisplayName("실패 기록이 없으면 잠기지 않는다")
  void isAccountLocked_NoRecord() {
    when(valueOperations.get("LF:" + EMAIL)).thenReturn(null);

    assertThat(refreshTokenService.isAccountLocked(EMAIL)).isFalse();
  }

  @Test
  @DisplayName("실패가 5회 미만이면 잠기지 않는다")
  void isAccountLocked_UnderLimit() {
    when(valueOperations.get("LF:" + EMAIL)).thenReturn("4");

    assertThat(refreshTokenService.isAccountLocked(EMAIL)).isFalse();
  }

  @Test
  @DisplayName("실패가 5회 이상이면 잠긴다")
  void isAccountLocked_AtLimit() {
    when(valueOperations.get("LF:" + EMAIL)).thenReturn("5");

    assertThat(refreshTokenService.isAccountLocked(EMAIL)).isTrue();
  }
}

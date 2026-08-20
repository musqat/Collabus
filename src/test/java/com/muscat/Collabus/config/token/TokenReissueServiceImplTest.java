package com.muscat.Collabus.config.token;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.muscat.Collabus.User.entity.User;
import com.muscat.Collabus.User.repository.UserRepository;
import com.muscat.Collabus.common.exception.BusinessException;
import com.muscat.Collabus.config.jwt.JwtUtil;
import com.muscat.Collabus.enums.role.SystemRole;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/**
 * 재발급 분기를 확인한다. Refresh Token Rotation 이라 저장된 토큰과 다르면 막음
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("토큰 재발급")
class TokenReissueServiceImplTest {

  private static final String EMAIL = "user@test.com";
  private static final String SAVED_TOKEN = "saved-refresh";

  @Mock
  private JwtUtil jwtUtil;

  @Mock
  private RefreshTokenService refreshTokenService;

  @Mock
  private UserRepository userRepository;

  @InjectMocks
  private TokenReissueServiceImpl tokenReissueService;

  @BeforeEach
  void setUp() {
    when(jwtUtil.validateToken(anyString())).thenReturn(true);
    when(jwtUtil.getEmailFromToken(anyString())).thenReturn(EMAIL);
    when(refreshTokenService.getRefreshToken(EMAIL)).thenReturn(Optional.of(SAVED_TOKEN));
    when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(User.builder()
        .id(1L).email(EMAIL).displayName("user#1234").role(SystemRole.USER).build()));
    when(jwtUtil.generateToken(anyLong(), anyString(), anyString(), anyString()))
        .thenReturn("new-access");
    when(jwtUtil.generateRefreshToken(EMAIL)).thenReturn("new-refresh");
    when(jwtUtil.getRefreshExpiration()).thenReturn(1000L);
  }

  @Test
  @DisplayName("검증에 실패한 토큰은 401 이다")
  void invalidToken() {
    when(jwtUtil.validateToken("bad")).thenReturn(false);

    assertThatThrownBy(() -> tokenReissueService.reissue("bad"))
        .isInstanceOf(BusinessException.class);

    verify(refreshTokenService, never()).saveRefreshToken(anyString(), anyString(), anyLong());
  }

  @Test
  @DisplayName("저장된 토큰이 없으면 401 이다")
  void noSavedToken() {
    when(refreshTokenService.getRefreshToken(EMAIL)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> tokenReissueService.reissue(SAVED_TOKEN))
        .isInstanceOf(BusinessException.class);
  }

  @Test
  @DisplayName("저장된 토큰과 다르면 401 이다")
  void rotatedAwayToken() {
    assertThatThrownBy(() -> tokenReissueService.reissue("older-refresh"))
        .isInstanceOf(BusinessException.class);

    verify(refreshTokenService, never()).saveRefreshToken(anyString(), anyString(), anyLong());
  }

  @Test
  @DisplayName("토큰의 사용자가 사라졌으면 401 이다")
  void userGone() {
    when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> tokenReissueService.reissue(SAVED_TOKEN))
        .isInstanceOf(BusinessException.class);
  }

  @Test
  @DisplayName("맞으면 새 토큰 쌍을 주고 저장된 것을 갈아끼운다")
  void success() {
    TokenResponseDto result = tokenReissueService.reissue(SAVED_TOKEN);

    assertThat(result.getAccessToken()).isEqualTo("new-access");
    assertThat(result.getRefreshToken()).isEqualTo("new-refresh");
    verify(refreshTokenService, times(1)).saveRefreshToken(EMAIL, "new-refresh", 1000L);
  }
}

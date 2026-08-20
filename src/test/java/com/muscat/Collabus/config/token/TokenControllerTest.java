package com.muscat.Collabus.config.token;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.muscat.Collabus.User.entity.User;
import com.muscat.Collabus.User.repository.UserRepository;
import com.muscat.Collabus.common.dto.ResponseDto;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * 재발급 분기를 확인한다. Refresh Token Rotation 이라 저장된 토큰과 다르면 막아야 한다.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("토큰 재발급")
class TokenControllerTest {

  private static final String EMAIL = "user@test.com";
  private static final String SAVED_TOKEN = "saved-refresh";

  @Mock
  private JwtUtil jwtUtil;

  @Mock
  private RefreshTokenService refreshTokenService;

  @Mock
  private UserRepository userRepository;

  @InjectMocks
  private TokenController tokenController;

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

  private RefreshRequestDto request(String token) {
    RefreshRequestDto dto = new RefreshRequestDto();
    ReflectionTestUtils.setField(dto, "refreshToken", token);
    return dto;
  }

  @Test
  @DisplayName("검증에 실패한 토큰은 401 이다")
  void invalidToken() {
    when(jwtUtil.validateToken("bad")).thenReturn(false);

    ResponseEntity<ResponseDto> response = tokenController.refreshToken(request("bad"));

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    verify(refreshTokenService, never()).saveRefreshToken(anyString(), anyString(), anyLong());
  }

  @Test
  @DisplayName("저장된 토큰이 없으면 401 이다")
  void noSavedToken() {
    when(refreshTokenService.getRefreshToken(EMAIL)).thenReturn(Optional.empty());

    assertThat(tokenController.refreshToken(request(SAVED_TOKEN)).getStatusCode())
        .isEqualTo(HttpStatus.UNAUTHORIZED);
  }

  @Test
  @DisplayName("저장된 토큰과 다르면 401 이다")
  void rotatedAwayToken() {
    ResponseEntity<ResponseDto> response = tokenController.refreshToken(request("older-refresh"));

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    verify(refreshTokenService, never()).saveRefreshToken(anyString(), anyString(), anyLong());
  }

  @Test
  @DisplayName("토큰의 사용자가 사라졌으면 401 이다")
  void userGone() {
    when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

    assertThat(tokenController.refreshToken(request(SAVED_TOKEN)).getStatusCode())
        .isEqualTo(HttpStatus.UNAUTHORIZED);
  }

  @Test
  @DisplayName("맞으면 새 토큰 쌍을 주고 저장된 것을 갈아끼운다")
  void success() {
    ResponseEntity<ResponseDto> response = tokenController.refreshToken(request(SAVED_TOKEN));

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    verify(refreshTokenService, times(1)).saveRefreshToken(EMAIL, "new-refresh", 1000L);
  }
}

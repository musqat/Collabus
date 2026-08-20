package com.muscat.Collabus.User.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.muscat.Collabus.User.model.UserResponseDto;
import com.muscat.Collabus.User.model.LoginDto;
import com.muscat.Collabus.User.service.UserService;
import com.muscat.Collabus.common.dto.ResponseDto;
import com.muscat.Collabus.common.exception.BusinessException;
import com.muscat.Collabus.config.jwt.JwtUtil;
import com.muscat.Collabus.config.token.RefreshTokenService;
import com.muscat.Collabus.enums.response.CommonResponse;
import com.muscat.Collabus.enums.role.SystemRole;

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
 * 로그인 분기를 확인한다
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("로그인 분기")
class UserControllerLoginTest {

  private static final String EMAIL = "user@test.com";
  private static final String PASSWORD = "password123";

  @Mock
  private UserService userService;

  @Mock
  private JwtUtil jwtUtil;

  @Mock
  private RefreshTokenService refreshTokenService;

  @InjectMocks
  private UserController userController;

  private LoginDto request;

  @BeforeEach
  void setUp() {
    request = new LoginDto();
    ReflectionTestUtils.setField(request, "email", EMAIL);
    ReflectionTestUtils.setField(request, "password", PASSWORD);

    when(refreshTokenService.isAccountLocked(anyString())).thenReturn(false);
    when(jwtUtil.generateToken(anyLong(), anyString(), anyString(), anyString()))
        .thenReturn("access");
    when(jwtUtil.generateRefreshToken(anyString())).thenReturn("refresh");
    when(jwtUtil.getRefreshExpiration()).thenReturn(1000L);
  }

  private UserResponseDto user() {
    return UserResponseDto.builder()
        .id(1L)
        .email(EMAIL)
        .nickname("user")
        .displayName("user#1234")
        .role(SystemRole.USER)
        .build();
  }

  @Test
  @DisplayName("잠긴 계정은 429 를 준다")
  void lockedAccount() {
    when(refreshTokenService.isAccountLocked(EMAIL)).thenReturn(true);

    ResponseEntity<ResponseDto> response = userController.login(request);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
    verify(userService, never()).login(anyString(), anyString());
  }

  @Test
  @DisplayName("성공하면 토큰을 담아 주고 실패 횟수를 지운다")
  void success() {
    when(userService.login(EMAIL, PASSWORD)).thenReturn(user());

    ResponseEntity<ResponseDto> response = userController.login(request);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    verify(refreshTokenService, times(1)).resetLoginFailure(EMAIL);
    verify(refreshTokenService, times(1)).saveRefreshToken(EMAIL, "refresh", 1000L);
    verify(refreshTokenService, never()).incrementLoginFailure(anyString());
  }

  @Test
  @DisplayName("없는 이메일은 404 를 주고 실패 횟수를 올리지 않는다")
  void emailNotFound() {
    when(userService.login(EMAIL, PASSWORD))
        .thenThrow(new BusinessException(CommonResponse.USER_NOT_FOUND));

    ResponseEntity<ResponseDto> response = userController.login(request);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    verify(refreshTokenService, never()).incrementLoginFailure(anyString());
  }

  @Test
  @DisplayName("비밀번호가 틀리면 401 을 주고 실패 횟수를 올린다")
  void wrongPassword() {
    when(userService.login(EMAIL, PASSWORD)).thenThrow(new IllegalArgumentException());
    when(refreshTokenService.incrementLoginFailure(EMAIL)).thenReturn(1);

    ResponseEntity<ResponseDto> response = userController.login(request);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    verify(refreshTokenService, times(1)).incrementLoginFailure(EMAIL);
  }

  @Test
  @DisplayName("네 번째 실패까지는 401 이다")
  void fourthFailure() {
    when(userService.login(EMAIL, PASSWORD)).thenThrow(new IllegalArgumentException());
    when(refreshTokenService.incrementLoginFailure(EMAIL)).thenReturn(4);

    assertThat(userController.login(request).getStatusCode())
        .isEqualTo(HttpStatus.UNAUTHORIZED);
  }

  @Test
  @DisplayName("다섯 번째 실패에서 429 로 바뀐다")
  void fifthFailure() {
    when(userService.login(EMAIL, PASSWORD)).thenThrow(new IllegalArgumentException());
    when(refreshTokenService.incrementLoginFailure(EMAIL)).thenReturn(5);

    assertThat(userController.login(request).getStatusCode())
        .isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
  }

  @Test
  @DisplayName("다섯 번을 넘겨도 429 를 유지한다")
  void beyondLimit() {
    when(userService.login(EMAIL, PASSWORD)).thenThrow(new IllegalArgumentException());
    when(refreshTokenService.incrementLoginFailure(EMAIL)).thenReturn(9);

    assertThat(userController.login(request).getStatusCode())
        .isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
  }
}

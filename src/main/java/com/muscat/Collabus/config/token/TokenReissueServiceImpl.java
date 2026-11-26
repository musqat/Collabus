package com.muscat.Collabus.config.token;

import com.muscat.Collabus.User.entity.User;
import com.muscat.Collabus.User.repository.UserRepository;
import com.muscat.Collabus.common.exception.BusinessException;
import com.muscat.Collabus.config.jwt.JwtUtil;
import com.muscat.Collabus.enums.response.CommonResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TokenReissueServiceImpl implements TokenReissueService {

  private final JwtUtil jwtUtil;
  private final RefreshTokenService refreshTokenService;
  private final UserRepository userRepository;

  @Override
  public TokenResponseDto reissue(String refreshToken) {
    if (!jwtUtil.validateToken(refreshToken)) {
      throw new BusinessException(CommonResponse.UNAUTHORIZED);
    }

    String email = jwtUtil.getEmailFromToken(refreshToken);
    String sessionId = jwtUtil.getSessionId(refreshToken);
    // 세션 ID 도입 전에 발급된 토큰은 다시 로그인
    if (sessionId == null) {
      throw new BusinessException(CommonResponse.UNAUTHORIZED);
    }

    // 그 세션에 저장된 것과 다르면 이미 로테이션으로 밀려난 토큰
    String saved = refreshTokenService.getRefreshToken(email, sessionId).orElse(null);
    if (saved == null || !saved.equals(refreshToken)) {
      throw new BusinessException(CommonResponse.UNAUTHORIZED);
    }

    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new BusinessException(CommonResponse.UNAUTHORIZED));

    String newAccessToken = jwtUtil.generateToken(
        user.getId(), email, user.getRole().name(), user.getDisplayName(), sessionId);

    // Refresh Token Rotation — 같은 세션의 RT 를 새것으로 바꿔 기존 것을 무효화
    String newRefreshToken = jwtUtil.generateRefreshToken(email, sessionId);
    refreshTokenService.saveRefreshToken(email, sessionId, newRefreshToken,
        jwtUtil.getRefreshExpiration());

    return new TokenResponseDto(newAccessToken, newRefreshToken);
  }
}

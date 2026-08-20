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

    // 저장된 것과 다르면 이미 로테이션으로 밀려난 토큰이다
    String saved = refreshTokenService.getRefreshToken(email).orElse(null);
    if (saved == null || !saved.equals(refreshToken)) {
      throw new BusinessException(CommonResponse.UNAUTHORIZED);
    }

    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new BusinessException(CommonResponse.UNAUTHORIZED));

    String newAccessToken = jwtUtil.generateToken(
        user.getId(), email, user.getRole().name(), user.getDisplayName());

    // Refresh Token Rotation — 새 RT 를 저장해 기존 것을 무효화한다
    String newRefreshToken = jwtUtil.generateRefreshToken(email);
    refreshTokenService.saveRefreshToken(email, newRefreshToken, jwtUtil.getRefreshExpiration());

    return new TokenResponseDto(newAccessToken, newRefreshToken);
  }
}

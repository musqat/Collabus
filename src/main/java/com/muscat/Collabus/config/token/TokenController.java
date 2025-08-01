package com.muscat.Collabus.config.token;

import com.muscat.Collabus.common.dto.ResponseDto;
import com.muscat.Collabus.config.jwt.JwtUtil;
import com.muscat.Collabus.enums.response.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/token")
@RequiredArgsConstructor
public class TokenController {

  private final JwtUtil jwtUtil;
  private final RefreshTokenService refreshTokenService;

  @Operation(summary = "Access Token 재발급", description = "Refresh Token으로 Access Token을 재발급합니다.")
  @PostMapping("/refresh")
  public ResponseEntity<ResponseDto> refreshToken(@RequestBody RefreshRequestDto request) {
    String refreshToken = request.getRefreshToken();

    if (!jwtUtil.validateToken(refreshToken)) {
      return ResponseEntity.status(401).body(new ResponseDto(CommonResponse.UNAUTHORIZED));
    }

    String email = jwtUtil.getEmailFromToken(refreshToken);
    String saved = refreshTokenService.getRefreshToken(email)
        .orElse(null);

    if (saved == null || !saved.equals(refreshToken)) {
      return ResponseEntity.status(401).body(new ResponseDto(CommonResponse.UNAUTHORIZED));
    }

    String newAccessToken = jwtUtil.generateToken(email, "USER");
    return ResponseEntity.ok(new ResponseDto(CommonResponse.SUCCESS,
        new TokenResponseDto(newAccessToken, refreshToken)));
  }
}

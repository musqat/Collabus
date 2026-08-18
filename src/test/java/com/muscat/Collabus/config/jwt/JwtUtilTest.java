package com.muscat.Collabus.config.jwt;

import static org.assertj.core.api.Assertions.assertThat;

import com.muscat.Collabus.config.security.CustomUserDetails;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("JwtUtil 단위 테스트")
class JwtUtilTest {

  private JwtUtil jwtUtil;

  @BeforeEach
  void setUp() {
    JwtProperties properties = new JwtProperties();
    properties.setSecret("test-only-secret-key-must-be-at-least-32-bytes-long");
    properties.setExpiration(900_000L);
    properties.setRefreshExpiration(604_800_000L);
    jwtUtil = new JwtUtil(properties);
  }

  @Test
  @DisplayName("같은 초에 발급해도 Refresh Token 이 서로 다르다")
  void refreshTokensAreUniqueWithinSameSecond() {
    // jti 가 없으면 iat/exp 가 초 단위라 토큰 문자열이 완전히 같아지고,
    // 그러면 Refresh Token Rotation 과 재사용 탐지가 무력화된다.
    String first = jwtUtil.generateRefreshToken("user@example.com");
    String second = jwtUtil.generateRefreshToken("user@example.com");

    assertThat(first).isNotEqualTo(second);
  }

  @Test
  @DisplayName("같은 초에 발급해도 Access Token 이 서로 다르다")
  void accessTokensAreUniqueWithinSameSecond() {
    // 토큰이 같으면 한 기기에서 로그아웃할 때 블랙리스트가 다른 세션까지 끊는다.
    String first = jwtUtil.generateToken(1L, "user@example.com", "USER", "user#0001");
    String second = jwtUtil.generateToken(1L, "user@example.com", "USER", "user#0001");

    assertThat(first).isNotEqualTo(second);
  }

  @Test
  @DisplayName("Access Token 은 인증에 필요한 정보를 모두 담는다")
  void accessTokenCarriesAuthenticationClaims() {
    String token = jwtUtil.generateToken(7L, "user@example.com", "USER", "user#0001");

    Claims claims = jwtUtil.parseClaims(token);

    assertThat(claims.getSubject()).isEqualTo("user@example.com");
    assertThat(claims.get(JwtUtil.CLAIM_USER_ID, Number.class).longValue()).isEqualTo(7L);
    assertThat(claims.get(JwtUtil.CLAIM_ROLE, String.class)).isEqualTo("USER");
    assertThat(claims.get(JwtUtil.CLAIM_DISPLAY_NAME, String.class)).isEqualTo("user#0001");
  }

  @Test
  @DisplayName("클레임만으로 인증 주체를 구성할 수 있다")
  void buildsPrincipalFromClaimsWithoutDatabase() {
    String token = jwtUtil.generateToken(7L, "user@example.com", "ADMIN", "admin#0000");

    CustomUserDetails principal = CustomUserDetails.from(jwtUtil.parseClaims(token));

    assertThat(principal.getUserId()).isEqualTo(7L);
    assertThat(principal.getUsername()).isEqualTo("user@example.com");
    assertThat(principal.getDisplayName()).isEqualTo("admin#0000");
    assertThat(principal.getAuthorities()).extracting("authority").containsExactly("ADMIN");
    // 인증 이후 단계에서 쓰이지 않으므로 비밀번호는 담지 않는다
    assertThat(principal.getPassword()).isNull();
  }

  @Test
  @DisplayName("위조된 토큰은 검증에 실패한다")
  void rejectsTamperedToken() {
    String token = jwtUtil.generateToken(1L, "user@example.com", "USER", "user#0001");
    String tampered = token.substring(0, token.length() - 2) + "xx";

    assertThat(jwtUtil.validateToken(tampered)).isFalse();
  }

  @Test
  @DisplayName("만료된 토큰은 검증에 실패한다")
  void rejectsExpiredToken() {
    JwtProperties expiredProperties = new JwtProperties();
    expiredProperties.setSecret("test-only-secret-key-must-be-at-least-32-bytes-long");
    expiredProperties.setExpiration(-1_000L);
    expiredProperties.setRefreshExpiration(-1_000L);
    JwtUtil expiredJwtUtil = new JwtUtil(expiredProperties);

    String expired = expiredJwtUtil.generateToken(1L, "user@example.com", "USER", "user#0001");

    assertThat(expiredJwtUtil.validateToken(expired)).isFalse();
  }

  @Test
  @DisplayName("다른 키로 서명된 토큰은 검증에 실패한다")
  void rejectsTokenSignedWithAnotherKey() {
    JwtProperties otherProperties = new JwtProperties();
    otherProperties.setSecret("another-secret-key-that-is-also-32-bytes-long!!");
    otherProperties.setExpiration(900_000L);
    otherProperties.setRefreshExpiration(604_800_000L);
    String foreign = new JwtUtil(otherProperties)
        .generateToken(1L, "user@example.com", "USER", "user#0001");

    assertThat(jwtUtil.validateToken(foreign)).isFalse();
  }
}

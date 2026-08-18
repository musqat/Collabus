package com.muscat.Collabus.config.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class JwtUtil {

  public static final String CLAIM_USER_ID = "userId";
  public static final String CLAIM_ROLE = "role";
  public static final String CLAIM_DISPLAY_NAME = "displayName";

  private final SecretKey key;
  private final long expiration;
  private final long refreshExpiration;

  public JwtUtil(JwtProperties jwtProperties) {
    this.key = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes());
    this.expiration = jwtProperties.getExpiration();
    this.refreshExpiration = jwtProperties.getRefreshExpiration();
  }

  /**
   * 인증에 필요한 정보를 모두 담는다. 요청마다 사용자를 다시 조회하지 않기 위함
   */
  public String generateToken(Long userId, String email, String role, String displayName) {
    Date now = new Date();
    Date expiry = new Date(now.getTime() + expiration);

    return Jwts.builder()
        .id(UUID.randomUUID().toString())
        .subject(email)
        .claim(CLAIM_USER_ID, userId)
        .claim(CLAIM_ROLE, role)
        .claim(CLAIM_DISPLAY_NAME, displayName)
        .issuedAt(now)
        .expiration(expiry)
        .signWith(key)
        .compact();
  }

  public String generateRefreshToken(String email) {
    Date now = new Date();
    Date expiry = new Date(now.getTime() + refreshExpiration);

    // jti 가 없으면 iat/exp 가 초 단위라 같은 초에 발급된 토큰이 문자열까지 동일해진다.
    // 그러면 Refresh Token Rotation 이 사실상 동작하지 않고 블랙리스트도 다른 세션에 번진다.
    return Jwts.builder()
        .id(UUID.randomUUID().toString())
        .subject(email)
        .issuedAt(now)
        .expiration(expiry)
        .signWith(key)
        .compact();
  }

  public boolean validateToken(String jwt) {
    try {
      parseClaims(jwt);
      return true;
    } catch (JwtException e) {
      log.warn("유효하지 않은 JWT: {}", e.getMessage());
      return false;
    }
  }

  public Claims parseClaims(String jwt) {
    return Jwts.parser()
        .verifyWith(key)
        .build()
        .parseSignedClaims(jwt)
        .getPayload();
  }

  public String getEmailFromToken(String jwt) {
    return parseClaims(jwt).getSubject();
  }

  public long getRefreshExpiration() {
    return refreshExpiration;
  }

  public long getRemainingMillis(String jwt) {
    return parseClaims(jwt).getExpiration().getTime() - System.currentTimeMillis();
  }
}

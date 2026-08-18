package com.muscat.Collabus.config.jwt;

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

  private final SecretKey key;
  private final long expiration;
  private final long refreshExpiration;

  public JwtUtil(JwtProperties jwtProperties) {
    this.key = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes());
    this.expiration = jwtProperties.getExpiration();
    this.refreshExpiration = jwtProperties.getRefreshExpiration();
  }

  public String generateToken(String email, String role) {
    Date now = new Date();
    Date expiry = new Date(now.getTime() + expiration);

    return Jwts.builder()
        .id(UUID.randomUUID().toString())
        .subject(email)
        .claim("role", role)
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
      Jwts.parser()
          .verifyWith(key)
          .build()
          .parseSignedClaims(jwt);
      return true;
    } catch (JwtException e) {
      log.warn("유효하지 않은 JWT: {}", e.getMessage());
      return false;
    }
  }

  public String getEmailFromToken(String jwt) {
    return Jwts.parser()
        .verifyWith(key)
        .build()
        .parseSignedClaims(jwt)
        .getPayload()
        .getSubject();
  }

  public long getRefreshExpiration() {
    return refreshExpiration;
  }

  public long getRemainingMillis(String jwt) {
    Date expiration = Jwts.parser()
        .verifyWith(key)
        .build()
        .parseSignedClaims(jwt)
        .getPayload()
        .getExpiration();

    return expiration.getTime() - System.currentTimeMillis();
  }
}

package com.muscat.Collabus.config.jwt;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Component;

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

    return Jwts.builder()
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
      System.out.println("Invalid JWT: " + e.getMessage());
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

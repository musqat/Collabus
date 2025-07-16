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

  public JwtUtil(JwtProperties jwtProperties) {
    this.key = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes());
    this.expiration = jwtProperties.getExpiration();
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

  public String getRoleFromToken(String jwt) {
    return String.valueOf(Jwts.parser()
        .verifyWith(key)
        .build()
        .parseSignedClaims(jwt)
        .getPayload()
        .get("role"));
  }
}

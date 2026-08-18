package com.muscat.Collabus.config.jwt;

import com.muscat.Collabus.config.security.CustomUserDetails;
import com.muscat.Collabus.config.token.RefreshTokenService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtUtil jwtUtil;
  private final RefreshTokenService refreshTokenService;

  public JwtAuthenticationFilter(JwtUtil jwtUtil, RefreshTokenService refreshTokenService) {
    this.jwtUtil = jwtUtil;
    this.refreshTokenService = refreshTokenService;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request,
      HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {

    final String token = extractToken(request);

    if (token != null) {
      if (refreshTokenService.isBlacklisted(token)) {
        log.warn("로그아웃된 토큰입니다. 401 반환.");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        return;
      }

      if (!jwtUtil.validateToken(token)) {
        log.warn("유효하지 않거나 만료된 JWT 토큰입니다. 401 반환.");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        return;
      }

      // 서명이 검증된 토큰의 클레임만으로 인증 주체를 구성
      Claims claims = jwtUtil.parseClaims(token);
      CustomUserDetails userDetails = CustomUserDetails.from(claims);

      UsernamePasswordAuthenticationToken authentication =
          new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

      SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    filterChain.doFilter(request, response);
  }

  //  Authorization header에서 Bearer token 추출
  private String extractToken(HttpServletRequest request) {
    String authHeader = request.getHeader("Authorization");
    if (authHeader != null && authHeader.startsWith("Bearer ")) {
      return authHeader.substring(7);
    }
    return null;
  }
}

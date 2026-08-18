package com.muscat.Collabus.config.jwt;

import com.muscat.Collabus.config.security.CustomUserDetails;
import com.muscat.Collabus.config.security.SecurityErrorResponder;
import com.muscat.Collabus.enums.response.ErrorType;
import com.muscat.Collabus.config.token.RefreshTokenService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtUtil jwtUtil;
  private final RefreshTokenService refreshTokenService;
  private final SecurityErrorResponder errorResponder;

  public JwtAuthenticationFilter(JwtUtil jwtUtil, RefreshTokenService refreshTokenService,
      SecurityErrorResponder errorResponder) {
    this.jwtUtil = jwtUtil;
    this.refreshTokenService = refreshTokenService;
    this.errorResponder = errorResponder;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request,
      HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {

    final String token = extractToken(request);

    if (token != null) {
      if (refreshTokenService.isBlacklisted(token)) {
        log.warn("로그아웃된 토큰입니다. 401 반환.");
        errorResponder.write(request, response, HttpStatus.UNAUTHORIZED,
            "로그아웃된 토큰입니다.", ErrorType.UNAUTHORIZED);
        return;
      }

      if (!jwtUtil.validateToken(token)) {
        log.warn("유효하지 않거나 만료된 JWT 토큰입니다. 401 반환.");
        errorResponder.write(request, response, HttpStatus.UNAUTHORIZED,
            "유효하지 않거나 만료된 토큰입니다.", ErrorType.UNAUTHORIZED);
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

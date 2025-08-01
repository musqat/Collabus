package com.muscat.Collabus.config.jwt;

import com.muscat.Collabus.config.security.CustomUserDetailsService;
import com.muscat.Collabus.config.token.RefreshTokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtUtil jwtUtil;
  private final CustomUserDetailsService userDetailsService;
  private final RefreshTokenService refreshTokenService;

  public JwtAuthenticationFilter(JwtUtil jwtUtil, CustomUserDetailsService userDetailsService,
      RefreshTokenService refreshTokenService) {
    this.jwtUtil = jwtUtil;
    this.userDetailsService = userDetailsService;
    this.refreshTokenService = refreshTokenService;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request,
      HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {

    final String token = extractToken(request);
    log.debug("🔥 JwtAuthenticationFilter 실행됨");
    log.debug("🔑 추출된 토큰: {}", token);

    if (token != null) {
      if (refreshTokenService.isBlacklisted(token)) {
        log.warn("블랙리스트 처리된 토큰 접근 차단");
        filterChain.doFilter(request, response);
        return;
      }
    }
    if (token != null && jwtUtil.validateToken(token)) {
      String email = jwtUtil.getEmailFromToken(token);
      UserDetails userDetails = userDetailsService.loadUserByUsername(email);

      UsernamePasswordAuthenticationToken authentication =
          new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

      SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    filterChain.doFilter(request, response);
  }

  //Authorization 헤더에서 Bearer 토큰 추출
  private String extractToken(HttpServletRequest request) {
    String authHeader = request.getHeader("Authorization");
    if (authHeader != null && authHeader.startsWith("Bearer ")) {
      return authHeader.substring(7);
    }
    return null;
  }
}

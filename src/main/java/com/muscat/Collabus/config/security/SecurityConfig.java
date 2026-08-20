package com.muscat.Collabus.config.security;


import com.muscat.Collabus.config.jwt.JwtAuthenticationFilter;
import com.muscat.Collabus.config.jwt.JwtUtil;
import com.muscat.Collabus.config.token.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;
import org.springframework.beans.factory.annotation.Value;


@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  @Value("${cors.allowed-origins:http://localhost:3000,http://localhost}")
  private List<String> allowedOrigins;

  @Value("${springdoc.api-docs.enabled:true}")
  private boolean apiDocsEnabled;

  private static final String[] API_DOCS_PATHS = {"/swagger-ui/**", "/v3/api-docs/**"};

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http,
      JwtUtil jwtUtil,
      RefreshTokenService refreshTokenService,
      SecurityErrorResponder errorResponder,
      RestAuthenticationEntryPoint authenticationEntryPoint,
      RestAccessDeniedHandler accessDeniedHandler) throws Exception {

    http
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
        .authorizeHttpRequests(auth -> {
          // API 문서는 켜져 있을 때만 연다
          if (apiDocsEnabled) {
            auth.requestMatchers(API_DOCS_PATHS).permitAll();
          }
          auth.requestMatchers(
                  "/ws/**",
                  "/actuator/health"
              ).permitAll()
              .requestMatchers(
                  "/api/users/login",
                  "/api/users/register",
                  "/api/token/**"
              ).permitAll()
              .anyRequest().authenticated();
        })
        // 기본값은 익명 요청에도 403 을 줌 -> 401/403 을 명시적으로 구분한다
        .exceptionHandling(ex -> ex
            .authenticationEntryPoint(authenticationEntryPoint)
            .accessDeniedHandler(accessDeniedHandler))
        .csrf(AbstractHttpConfigurer::disable)
        .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
        .addFilterBefore(new JwtAuthenticationFilter(jwtUtil, refreshTokenService, errorResponder),
            UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    allowedOrigins.forEach(configuration::addAllowedOrigin);
    configuration.addAllowedMethod("*");
    configuration.addAllowedHeader("*");
    configuration.setAllowCredentials(true);
    configuration.setExposedHeaders(List.of("Authorization"));

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }
}

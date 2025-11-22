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

import java.util.Arrays;


@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http,
      JwtUtil jwtUtil,
      CustomUserDetailsService userDetailsService,
      RefreshTokenService refreshTokenService) throws Exception {

    http
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
        .authorizeHttpRequests(auth -> auth
            .requestMatchers(
                "/swagger-ui/**",
                "/v3/api-docs/**",
                "/h2-console/**",
                "/ws/**"
            ).permitAll()
            .requestMatchers(
                "/api/users/login",
                "/api/users/register",
                "/api/token/**"
            ).permitAll()
            .anyRequest().authenticated()
        )
        .csrf(AbstractHttpConfigurer::disable)
        .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
        .addFilterBefore(new JwtAuthenticationFilter(jwtUtil, userDetailsService, refreshTokenService),
            UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.addAllowedOrigin("http://localhost:3000"); // React 개발 서버
    configuration.addAllowedOrigin("http://localhost"); // Docker Nginx (포트 80)
    configuration.addAllowedOrigin("http://localhost:80"); // Docker Nginx (명시적 포트)
    configuration.addAllowedMethod("*");
    configuration.addAllowedHeader("*");
    configuration.setAllowCredentials(true);
    configuration.setExposedHeaders(Arrays.asList("Authorization"));

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }
}

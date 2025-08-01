package com.muscat.Collabus.config;

import com.muscat.Collabus.config.security.CustomUserDetails;
import java.util.Optional;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class AuditorAwareImpl implements AuditorAware<String> {

  @Override
  public Optional<String> getCurrentAuditor() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if (authentication == null || !authentication.isAuthenticated()) {
      return Optional.of("system"); // 비로그인 상황 fallback
    }

    Object principal = authentication.getPrincipal();

    if (principal instanceof CustomUserDetails customUserDetails) {
      return Optional.of(customUserDetails.getUsername()); // 이메일 반환
    }

    return Optional.of(authentication.getName());
  }
}

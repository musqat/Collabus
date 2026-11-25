package com.muscat.Collabus.support;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

import com.muscat.Collabus.User.entity.User;
import com.muscat.Collabus.config.security.CustomUserDetails;
import com.muscat.Collabus.enums.role.SystemRole;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

/**
 * 컨트롤러 슬라이스 테스트용 로그인 사용자
 */
public final class LoginUser {

  public static final Long ID = 7L;
  public static final String EMAIL = "login@test.com";
  public static final Long ADMIN_ID = 1L;

  private LoginUser() {
  }

  // 일반 사용자로 로그인한 요청
  public static RequestPostProcessor login() {
    return as(ID, EMAIL, SystemRole.USER);
  }

  // 관리자로 로그인한 요청
  public static RequestPostProcessor admin() {
    return as(ADMIN_ID, "admin@test.com", SystemRole.ADMIN);
  }

  private static RequestPostProcessor as(Long id, String email, SystemRole role) {
    return user(new CustomUserDetails(User.builder()
        .id(id)
        .email(email)
        .password("unused")
        .nickname("login")
        .tag("0007")
        .displayName("login#0007")
        .role(role)
        .build()));
  }
}

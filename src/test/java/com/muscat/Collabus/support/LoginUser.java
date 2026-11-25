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

  private LoginUser() {
  }

  // 요청에 로그인 사용자를 실음
  public static RequestPostProcessor login() {
    return user(new CustomUserDetails(User.builder()
        .id(ID)
        .email("login@test.com")
        .password("unused")
        .nickname("login")
        .tag("0007")
        .displayName("login#0007")
        .role(SystemRole.USER)
        .build()));
  }
}

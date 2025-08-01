package com.muscat.Collabus.config;

import com.muscat.Collabus.User.entity.User;
import com.muscat.Collabus.User.repository.UserRepository;
import com.muscat.Collabus.enums.role.SystemRole;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminAccountInitializer implements CommandLineRunner {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  @Override
  public void run(String... args) {
    String email = "admin@collabus.com";
    String nickname = "admin";
    String tag = "0000";
    String displayName = nickname + "#" + tag;

    if (!userRepository.existsByDisplayName(displayName)) {
      User admin = User.builder()
          .email(email)
          .nickname(nickname)
          .password(passwordEncoder.encode("admin1234"))
          .tag(tag)
          .displayName(displayName)
          .role(SystemRole.ADMIN)
          .build();

      userRepository.save(admin);
      System.out.println("기본 관리자 계정(admin#0000) 생성 완료");
    }
  }
}

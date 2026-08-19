package com.muscat.Collabus.User.mapper;

import com.muscat.Collabus.User.model.UserSummaryDto;
import com.muscat.Collabus.User.entity.User;
import com.muscat.Collabus.User.model.UserRequestDto;
import com.muscat.Collabus.User.model.UserResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserMapper {

  private final PasswordEncoder passwordEncoder;

  // 요청 DTO → Entity (회원가입용)
  public User mapToEntity(UserRequestDto requestDto, String tag) {
    String displayName = requestDto.getNickname() + "#" + tag;

    return User.builder()
        .email(requestDto.getEmail())
        .nickname(requestDto.getNickname())
        .password(passwordEncoder.encode(requestDto.getPassword()))
        .tag(tag)
        .displayName(displayName)
        .build();
  }

  // 검색 결과. 이메일과 시스템 역할은 담지 않는다
  public UserSummaryDto mapToSummary(User user) {
    return UserSummaryDto.builder()
        .id(user.getId())
        .nickname(user.getNickname())
        .displayName(user.getDisplayName())
        .build();
  }

  // Entity → 응답 DTO
  public UserResponseDto mapToDto(User user) {
    return UserResponseDto.builder()
        .id(user.getId())
        .email(user.getEmail())
        .nickname(user.getNickname())
        .displayName(user.getDisplayName())
        .role(user.getRole())
        .build();
  }

}

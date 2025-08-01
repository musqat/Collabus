package com.muscat.Collabus.User.mapper;

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

  // Entity → 응답 DTO
  public UserResponseDto mapToResponseDto(User user) {
    return UserResponseDto.builder()
        .id(user.getId())
        .email(user.getEmail())
        .displayName(user.getDisplayName())
        .role(user.getRole())
        .build();
  }

  // 업데이트용
  public void updateNickname(User user, String newNickname) {
    String newDisplayName = newNickname + "#" + user.getTag();
    user.setNickname(newNickname);
    user.setDisplayName(newDisplayName);
  }

  public void updatePassword(User user, String newPassword) {
    user.setPassword(passwordEncoder.encode(newPassword));
  }
}

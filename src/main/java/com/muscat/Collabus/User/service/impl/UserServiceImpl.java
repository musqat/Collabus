package com.muscat.Collabus.User.service.impl;

import com.muscat.Collabus.User.entity.User;
import com.muscat.Collabus.User.mapper.UserMapper;
import com.muscat.Collabus.User.model.UserRequestDto;
import com.muscat.Collabus.User.model.UserResponseDto;
import com.muscat.Collabus.User.repository.UserRepository;
import com.muscat.Collabus.User.service.UserService;
import com.muscat.Collabus.common.exception.ResourceAlreadyExistsException;
import com.muscat.Collabus.common.exception.ResourceNotFoundException;
import com.muscat.Collabus.common.util.DisplayNameUtil;
import com.muscat.Collabus.enums.response.CommonResponse;
import com.muscat.Collabus.enums.response.UserResponse;
import com.muscat.Collabus.enums.role.SystemRole;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final UserMapper userMapper;

  @Override
  public void registerUser(UserRequestDto userDto) {
    if (userRepository.findByEmail(userDto.getEmail()).isPresent()) {
      throw new ResourceAlreadyExistsException(UserResponse.EMAIL_ALREADY_EXISTS);
    }

    String tag = DisplayNameUtil.generateUniqueTag(
        userDto.getNickname(),
        userRepository::existsByDisplayName
    );
    String displayName = userDto.getNickname() + "#" + tag;

    User user = User.builder()
        .email(userDto.getEmail())
        .nickname(userDto.getNickname())
        .password(passwordEncoder.encode(userDto.getPassword()))
        .role(SystemRole.USER)
        .tag(tag)
        .displayName(displayName)
        .build();

    userRepository.save(user);
  }

  @Override
  public List<UserResponseDto> searchByNickname(String keyword) {
    return userRepository.findByNicknameContainingIgnoreCase(keyword).stream()
        .map(userMapper::mapToResponseDto)
        .collect(Collectors.toList());
  }

  @Override
  public Optional<UserResponseDto> findByDisplayName(String displayName) {
    return userRepository.findByDisplayName(displayName)
        .map(userMapper::mapToResponseDto);
  }

  @Override
  public void updateNickname(Long userId, String newNickname) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException(CommonResponse.RESOURCE_NOT_FOUND));

    if (!newNickname.equals(user.getNickname())) {
      String newDisplayName = newNickname + "#" + user.getTag();

      if (userRepository.existsByDisplayNameAndIdNot(newDisplayName, userId)) {
        throw new ResourceAlreadyExistsException(UserResponse.NICKNAME_ALREADY_EXISTS);
      }

      user.setNickname(newNickname);
      user.setDisplayName(newDisplayName);
      userRepository.save(user);
    }
  }

  @Override
  public void updatePassword(Long userId, String newPassword) {
    if (newPassword == null || newPassword.isBlank()) {
      throw new IllegalArgumentException("변경할 비밀번호가 비어 있습니다.");
    }

    User user = userRepository.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException(CommonResponse.RESOURCE_NOT_FOUND));

    user.setPassword(passwordEncoder.encode(newPassword));
    userRepository.save(user);
  }

  @Override
  public boolean deleteUser(String email) {
    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new ResourceNotFoundException(CommonResponse.RESOURCE_NOT_FOUND));

    userRepository.delete(user);
    return true;
  }

  @Override
  public UserResponseDto login(String email, String password) {
    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new ResourceNotFoundException(CommonResponse.RESOURCE_NOT_FOUND));

    if (!passwordEncoder.matches(password, user.getPassword())) {
      throw new IllegalArgumentException(CommonResponse.UNAUTHORIZED.getMessage());
    }

    return userMapper.mapToResponseDto(user);
  }

  @Override
  public void createAdmin(UserRequestDto dto) {
    if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
      throw new ResourceAlreadyExistsException(UserResponse.EMAIL_ALREADY_EXISTS);
    }

    String tag = DisplayNameUtil.generateUniqueTag(
        dto.getNickname(),
        displayName -> userRepository.existsByDisplayName(displayName)
    );
    String displayName = dto.getNickname() + "#" + tag;

    User admin = User.builder()
        .email(dto.getEmail())
        .nickname(dto.getNickname())
        .password(passwordEncoder.encode(dto.getPassword()))
        .role(SystemRole.ADMIN)
        .tag(tag)
        .displayName(displayName)
        .build();

    userRepository.save(admin);
  }


}

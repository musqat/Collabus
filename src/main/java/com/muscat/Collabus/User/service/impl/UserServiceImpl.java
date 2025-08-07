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

    User user = userMapper.mapToEntity(userDto, tag);
    user.setRole(SystemRole.USER);

    userRepository.save(user);
  }

  @Override
  public List<UserResponseDto> searchByNickname(String keyword) {
    return userRepository.findByNicknameContainingIgnoreCase(keyword).stream()
        .map(userMapper::mapToDto)
        .collect(Collectors.toList());
  }

  @Override
  public Optional<UserResponseDto> findByDisplayName(String displayName) {
    return userRepository.findByDisplayName(displayName)
        .map(userMapper::mapToDto);
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

      user.changeNickname(newNickname);
      userRepository.save(user);
    }
  }

  @Override
  public void updatePassword(Long userId, String newPassword) {
    if (newPassword == null || newPassword.isBlank()) {
      throw new IllegalArgumentException(UserResponse.PASSWORD_BLANK.getMessage());
    }

    User user = userRepository.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException(CommonResponse.RESOURCE_NOT_FOUND));

    user.changePassword(newPassword, passwordEncoder);
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

    return userMapper.mapToDto(user);
  }

  @Override
  public void createAdmin(UserRequestDto userDto) {
    if (userRepository.findByEmail(userDto.getEmail()).isPresent()) {
      throw new ResourceAlreadyExistsException(UserResponse.EMAIL_ALREADY_EXISTS);
    }

    String tag = DisplayNameUtil.generateUniqueTag(
        userDto.getNickname(),
        userRepository::existsByDisplayName
    );

    User admin = userMapper.mapToEntity(userDto, tag);
    admin.setRole(SystemRole.ADMIN);

    userRepository.save(admin);
  }

}

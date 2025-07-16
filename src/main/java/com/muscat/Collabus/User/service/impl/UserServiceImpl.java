package com.muscat.Collabus.User.service.impl;

import com.muscat.Collabus.User.entity.User;
import com.muscat.Collabus.User.exception.UserAlreadyExistsException;
import com.muscat.Collabus.User.mapper.UserMapper;
import com.muscat.Collabus.User.model.UserDto;
import com.muscat.Collabus.User.repository.UserRepository;
import com.muscat.Collabus.User.service.UserService;
import com.muscat.Collabus.enums.SystemRole;
import com.muscat.Collabus.enums.UserResponse;
import java.util.List;
import java.util.Optional;
import java.util.Random;
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
  public UserDto registerUser(UserDto userDto) {
    if (userRepository.findByEmail(userDto.getEmail()).isPresent()) {
      throw new UserAlreadyExistsException(UserResponse.EMAIL_ALREADY_EXISTS);
    }
    User user = userMapper.mapToUser(new User(), userDto);
    user.setPassword(passwordEncoder.encode(userDto.getPassword()));
    user.setRole(SystemRole.USER);

    String tag = generateUniqueTag(user.getNickname());
    user.setTag(tag);
    user.setDisplayName(user.getNickname() + "#" + tag);

    userRepository.save(user);
    return userMapper.mapToUserDto(user);
  }


  @Override
  public List<UserDto> searchByNickname(String keyword) {
    return userRepository.findByNicknameContainingIgnoreCase(keyword).stream()
        .map(user -> userMapper.mapToUserDto(user))
        .collect(Collectors.toList());
  }

  @Override
  public Optional<UserDto> findByDisplayName(String displayName) {
    return userRepository.findByDisplayName(displayName)
        .map(user -> userMapper.mapToUserDto(user));
  }

  @Override
  public boolean updateUser(UserDto dto) {
    User user = userRepository.findByEmail(dto.getEmail())
        .orElseThrow(() -> new IllegalArgumentException(UserResponse.USER_NOT_FOUND.getMessage()));

    user = userMapper.mapToUser(user, dto);
    user.setPassword(passwordEncoder.encode(dto.getPassword()));

    String newDisplayName = user.getNickname() + "#" + user.getTag();
    if (!newDisplayName.equals(user.getDisplayName())) {
      if (userRepository.existsByDisplayName(newDisplayName)) {
        throw new UserAlreadyExistsException(UserResponse.NICKNAME_ALREADY_EXISTS);
      }
      user.setDisplayName(newDisplayName);
    }

    userRepository.save(user);
    return true;
  }

  @Override
  public boolean deleteUser(String email) {
    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new IllegalArgumentException(UserResponse.USER_NOT_FOUND.getMessage()));
    userRepository.delete(user);
    return true;
  }

  @Override
  public User login(String email, String password) {
    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new IllegalArgumentException(UserResponse.USER_NOT_FOUND.getMessage()));

    if (!passwordEncoder.matches(password, user.getPassword())) {
      throw new IllegalArgumentException(UserResponse.LOGIN_FAILED.getMessage());
    }

    return user;
  }


  private String generateUniqueTag(String nickname) {
    String tag;
    String displayName;
    do {
      tag = String.format("%04d", new Random().nextInt(10000));
      displayName = nickname + "#" + tag;
    } while (userRepository.existsByDisplayName(displayName));
    return tag;
  }
}

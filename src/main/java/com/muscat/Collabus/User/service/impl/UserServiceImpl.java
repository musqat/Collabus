package com.muscat.Collabus.User.service.impl;

import com.muscat.Collabus.User.entity.User;
import com.muscat.Collabus.User.mapper.UserMapper;
import com.muscat.Collabus.User.model.UserRequestDto;
import com.muscat.Collabus.User.model.UserResponseDto;
import com.muscat.Collabus.User.repository.UserRepository;
import com.muscat.Collabus.User.service.UserService;
import com.muscat.Collabus.common.exception.BusinessException;
import com.muscat.Collabus.common.exception.ResourceAlreadyExistsException;
import com.muscat.Collabus.common.exception.ResourceNotFoundException;
import com.muscat.Collabus.common.util.DisplayNameUtil;
import com.muscat.Collabus.config.token.RefreshTokenService;
import com.muscat.Collabus.enums.response.CommonResponse;
import com.muscat.Collabus.enums.response.UserResponse;
import com.muscat.Collabus.enums.role.SystemRole;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final RefreshTokenService refreshTokenService;

    // displayName 은 nickname#tag 형식이고, tag 는 중복되지 않게 자동 생성한다
    @Override
    @Transactional
    public void registerUser(UserRequestDto userDto) {
        if (userRepository.findByEmail(userDto.getEmail()).isPresent()) {
            throw new ResourceAlreadyExistsException(UserResponse.EMAIL_ALREADY_EXISTS);
        }

        String tag = DisplayNameUtil.generateUniqueTag(
                userDto.getNickname(),
                userRepository::existsByDisplayName
        );

        User user = userMapper.mapToEntity(userDto, tag);
        user.assignRole(SystemRole.USER);

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
    @Transactional
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
    @Transactional
    public void updatePassword(Long userId, String currentPassword, String newPassword) {
        if (newPassword == null || newPassword.isBlank()) {
            throw new IllegalArgumentException(UserResponse.PASSWORD_BLANK.getMessage());
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(CommonResponse.RESOURCE_NOT_FOUND));

        // 토큰만 탈취해도 비밀번호를 바꿀 수 있으면 계정이 그대로 넘어가므로 현재 비밀번호를 확인한다
        if (currentPassword == null || !passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new BusinessException(UserResponse.CURRENT_PASSWORD_MISMATCH);
        }

        user.changePassword(newPassword, passwordEncoder);
        userRepository.save(user);

        // 비밀번호 변경 시 기존 Refresh Token 무효화 → 다른 기기 세션 강제 종료
        refreshTokenService.deleteRefreshToken(user.getEmail());
    }

    @Override
    @Transactional
    public boolean deleteUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(CommonResponse.RESOURCE_NOT_FOUND));

        userRepository.delete(user);
        return true;
    }

    // 비밀번호 불일치는 IllegalArgumentException. 실패 횟수 집계는 컨트롤러가 담당한다
    @Override
    public UserResponseDto login(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(UserResponse.EMAIL_NOT_FOUND));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException(UserResponse.INVALID_PASSWORD.getMessage());
        }

        return userMapper.mapToDto(user);
    }

    // ADMIN 역할로 생성. 호출은 기존 ADMIN 만 가능하다
    @Override
    @Transactional
    public void createAdmin(UserRequestDto userDto) {
        if (userRepository.findByEmail(userDto.getEmail()).isPresent()) {
            throw new ResourceAlreadyExistsException(UserResponse.EMAIL_ALREADY_EXISTS);
        }

        String tag = DisplayNameUtil.generateUniqueTag(
                userDto.getNickname(),
                userRepository::existsByDisplayName
        );

        User admin = userMapper.mapToEntity(userDto, tag);
        admin.assignRole(SystemRole.ADMIN);

        userRepository.save(admin);
    }

}

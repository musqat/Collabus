package com.muscat.Collabus.User.service.impl;

import java.util.UUID;
import com.muscat.Collabus.enums.role.WorkspaceRole;
import com.muscat.Collabus.WorkspaceUser.service.WorkspaceUserService;
import com.muscat.Collabus.WorkspaceUser.repository.WorkspaceUserRepository;
import com.muscat.Collabus.WorkspaceUser.repository.WorkspaceInviteRepository;
import com.muscat.Collabus.Task.repository.TaskRepository;
import com.muscat.Collabus.Task.entity.Task;
import com.muscat.Collabus.Notification.repository.NotificationRepository;
import org.springframework.data.domain.Pageable;
import com.muscat.Collabus.common.util.SortGuard;
import com.muscat.Collabus.common.dto.PageResponseDto;
import com.muscat.Collabus.User.model.UserSummaryDto;
import com.muscat.Collabus.User.entity.User;
import com.muscat.Collabus.User.mapper.UserMapper;
import com.muscat.Collabus.User.model.UserRequestDto;
import com.muscat.Collabus.User.model.UserResponseDto;
import com.muscat.Collabus.User.repository.UserRepository;
import com.muscat.Collabus.User.service.UserService;
import com.muscat.Collabus.common.exception.BusinessException;
import com.muscat.Collabus.common.util.DisplayNameUtil;
import com.muscat.Collabus.config.token.RefreshTokenService;
import com.muscat.Collabus.enums.response.CommonResponse;
import com.muscat.Collabus.enums.response.UserResponse;
import com.muscat.Collabus.enums.role.SystemRole;

import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    // 한두 글자로는 전체 명부가 나오므로 최소 길이를 요구한다
    private static final int MIN_KEYWORD_LENGTH = 2;

    private final SortGuard sortGuard;
    private final WorkspaceUserRepository workspaceUserRepository;
    private final WorkspaceUserService workspaceUserService;
    private final TaskRepository taskRepository;
    private final NotificationRepository notificationRepository;
    private final WorkspaceInviteRepository inviteRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final RefreshTokenService refreshTokenService;

    @Override
    @Transactional
    public void registerUser(UserRequestDto userDto) {
        if (userRepository.findByEmail(userDto.getEmail()).isPresent()) {
            throw new BusinessException(UserResponse.EMAIL_ALREADY_EXISTS);
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
    public PageResponseDto<UserSummaryDto> searchByNickname(String keyword, Pageable pageable) {
        String trimmed = keyword == null ? "" : keyword.trim();
        if (trimmed.length() < MIN_KEYWORD_LENGTH) {
            throw new BusinessException(UserResponse.SEARCH_KEYWORD_TOO_SHORT);
        }

        return PageResponseDto.of(
                userRepository.findByNicknameContainingIgnoreCaseAndDeletedAtIsNull(trimmed,
                        sortGuard.apply(pageable, User.class)),
                userMapper::mapToSummary);
    }

    @Override
    public Optional<UserSummaryDto> findByDisplayName(String displayName) {
        return userRepository.findByDisplayName(displayName)
                .filter(found -> !found.isDeleted())
                .map(userMapper::mapToSummary);
    }

    @Override
    @Transactional
    public void updateNickname(Long userId, String newNickname) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(CommonResponse.RESOURCE_NOT_FOUND));

        if (!newNickname.equals(user.getNickname())) {
            String newDisplayName = newNickname + "#" + user.getTag();

            if (userRepository.existsByDisplayNameAndIdNot(newDisplayName, userId)) {
                throw new BusinessException(UserResponse.NICKNAME_ALREADY_EXISTS);
            }

            user.changeNickname(newNickname);
            userRepository.save(user);
        }
    }

    @Override
    @Transactional
    public void updatePassword(Long userId, String currentPassword, String newPassword) {
        if (newPassword == null || newPassword.isBlank()) {
            throw new BusinessException(UserResponse.PASSWORD_BLANK);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(CommonResponse.RESOURCE_NOT_FOUND));

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
                .filter(found -> !found.isDeleted())
                .orElseThrow(() -> new BusinessException(CommonResponse.RESOURCE_NOT_FOUND));

        leaveAllWorkspaces(user.getId());
        handOverManagedTasks(user.getId());
        notificationRepository.deleteByUser_Id(user.getId());
        inviteRepository.deleteByInviteeIdOrInviterId(user.getId(), user.getId());

        user.withdraw(
                "withdrawn-" + user.getId() + "@removed.local",
                "탈퇴한 사용자#" + user.getTag() + "-" + user.getId(),
                passwordEncoder.encode(UUID.randomUUID().toString()));
        userRepository.save(user);
        return true;
    }

    /**
     * 참여 중인 워크스페이스에서 모두 나간다.
     * 마지막 멤버면 워크스페이스까지 지우고, MASTER 면 남은 멤버가 승계한다.
     */
    private void leaveAllWorkspaces(Long userId) {
        workspaceUserRepository.findAllById_UserId(userId).stream()
                .map(member -> member.getId().getWorkspaceId())
                .toList()
                .forEach(workspaceId -> workspaceUserService.leaveWorkspace(workspaceId, userId));
    }

    /**
     * 맡고 있던 Task 의 매니저를 워크스페이스 MASTER 에게 넘긴다.
     * 매니저가 비면 그 Task 는 아무도 수정하거나 지울 수 없다.
     */
    private void handOverManagedTasks(Long userId) {
        for (Task task : taskRepository.findAllByTaskManager_Id(userId)) {
            workspaceUserRepository
                    .findFirstById_WorkspaceIdAndRole(
                            task.getWorkspace().getId(), WorkspaceRole.MASTER)
                    .ifPresent(master -> task.changeManager(master.getUser()));
        }
    }

    @Override
    public UserResponseDto login(String email, String password) {
        User user = userRepository.findByEmail(email)
                .filter(found -> !found.isDeleted())
                .orElseThrow(() -> new BusinessException(UserResponse.EMAIL_NOT_FOUND));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BusinessException(UserResponse.INVALID_PASSWORD);
        }

        return userMapper.mapToDto(user);
    }

    @Override
    @Transactional
    public void createAdmin(UserRequestDto userDto) {
        if (userRepository.findByEmail(userDto.getEmail()).isPresent()) {
            throw new BusinessException(UserResponse.EMAIL_ALREADY_EXISTS);
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

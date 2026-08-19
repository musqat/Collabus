package com.muscat.Collabus.User.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.muscat.Collabus.enums.role.WorkspaceRole;
import com.muscat.Collabus.WorkspaceUser.entity.WorkspaceUserPk;
import com.muscat.Collabus.WorkspaceUser.entity.WorkspaceUser;
import com.muscat.Collabus.Workspace.entity.Workspace;
import com.muscat.Collabus.Task.entity.Task;
import com.muscat.Collabus.WorkspaceUser.repository.WorkspaceInviteRepository;
import com.muscat.Collabus.Notification.repository.NotificationRepository;
import com.muscat.Collabus.Task.repository.TaskRepository;
import com.muscat.Collabus.WorkspaceUser.service.WorkspaceUserService;
import com.muscat.Collabus.WorkspaceUser.repository.WorkspaceUserRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.PageImpl;
import com.muscat.Collabus.common.util.SortGuard;
import com.muscat.Collabus.common.dto.PageResponseDto;
import com.muscat.Collabus.User.model.UserSummaryDto;
import com.muscat.Collabus.User.entity.User;
import com.muscat.Collabus.config.token.RefreshTokenService;
import com.muscat.Collabus.User.mapper.UserMapper;
import com.muscat.Collabus.User.model.UserRequestDto;
import com.muscat.Collabus.User.model.UserResponseDto;
import com.muscat.Collabus.User.repository.UserRepository;
import com.muscat.Collabus.common.exception.BusinessException;
import com.muscat.Collabus.enums.role.SystemRole;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService 단위 테스트")
class UserServiceImplTest {

    @Mock

    private WorkspaceUserRepository workspaceUserRepository;


    @Mock

    private WorkspaceUserService workspaceUserService;


    @Mock

    private TaskRepository taskRepository;


    @Mock

    private NotificationRepository notificationRepository;


    @Mock

    private WorkspaceInviteRepository inviteRepository;


    @Mock

    private SortGuard sortGuard;


    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserMapper userMapper;

    @Mock
    private RefreshTokenService refreshTokenService;

    @InjectMocks
    private UserServiceImpl userService;

    private UserRequestDto userRequestDto;
    private User user;
    private UserResponseDto userResponseDto;
    private UserSummaryDto userSummaryDto;

    @BeforeEach
    void setUp() {
        userRequestDto = UserRequestDto.builder()
                .email("test@example.com")
                .nickname("testuser")
                .password("password123")
                .build();

        user = User.builder()
                .id(1L)
                .email("test@example.com")
                .nickname("testuser")
                .password("encodedPassword")
                .tag("1234")
                .displayName("testuser#1234")
                .role(SystemRole.USER)
                .build();

        userResponseDto = UserResponseDto.builder()
                .id(1L)
                .email("test@example.com")
                .displayName("testuser#1234")
                .role(SystemRole.USER)
                .build();


        userSummaryDto = UserSummaryDto.builder()
                .id(1L)
                .nickname("testuser")
                .displayName("testuser#1234")
                .build();
    }

    @Test
    @DisplayName("회원가입 성공")
    void registerUser_Success() {
        // Given
        when(userRepository.findByEmail(userRequestDto.getEmail())).thenReturn(Optional.empty());
        when(userRepository.existsByDisplayName(anyString())).thenReturn(false);
        when(userMapper.mapToEntity(any(UserRequestDto.class), anyString())).thenReturn(user);
        when(userRepository.save(any(User.class))).thenReturn(user);

        // When
        userService.registerUser(userRequestDto);

        // Then
        verify(userRepository, times(1)).findByEmail(userRequestDto.getEmail());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("회원가입 실패 - 이메일 중복")
    void registerUser_Fail_EmailExists() {
        // Given
        when(userRepository.findByEmail(userRequestDto.getEmail())).thenReturn(Optional.of(user));

        // When & Then
        assertThatThrownBy(() -> userService.registerUser(userRequestDto))
                .isInstanceOf(BusinessException.class);

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("닉네임으로 사용자 검색 성공")
    void searchByNickname_Success() {
        // Given
        String keyword = "test";
        Pageable pageable = PageRequest.of(0, 20);
        when(sortGuard.apply(pageable, User.class)).thenReturn(pageable);
        when(userRepository.findByNicknameContainingIgnoreCaseAndDeletedAtIsNull(keyword, pageable))
                .thenReturn(new PageImpl<>(List.of(user), pageable, 1));
        when(userMapper.mapToSummary(user)).thenReturn(userSummaryDto);

        // When
        PageResponseDto<UserSummaryDto> result = userService.searchByNickname(keyword, pageable);

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getDisplayName()).isEqualTo("testuser#1234");
    }

    @Test
    @DisplayName("검색어가 두 글자보다 짧으면 거부한다")
    void searchByNickname_Fail_TooShort() {
        assertThatThrownBy(() -> userService.searchByNickname("a", PageRequest.of(0, 20)))
                .isInstanceOf(BusinessException.class);

        verify(userRepository, times(0))
                .findByNicknameContainingIgnoreCase(any(), any());
    }

    @Test
    @DisplayName("공백만 있는 검색어도 거부한다")
    void searchByNickname_Fail_Blank() {
        assertThatThrownBy(() -> userService.searchByNickname("   ", PageRequest.of(0, 20)))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("DisplayName으로 사용자 찾기 성공")
    void findByDisplayName_Success() {
        // Given
        String displayName = "testuser#1234";
        when(userRepository.findByDisplayName(displayName)).thenReturn(Optional.of(user));
        when(userMapper.mapToSummary(user)).thenReturn(userSummaryDto);

        // When
        Optional<UserSummaryDto> result = userService.findByDisplayName(displayName);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getDisplayName()).isEqualTo(displayName);
    }

    @Test
    @DisplayName("DisplayName으로 사용자 찾기 실패 - 존재하지 않음")
    void findByDisplayName_NotFound() {
        // Given
        String displayName = "nonexistent#0000";
        when(userRepository.findByDisplayName(displayName)).thenReturn(Optional.empty());

        // When
        Optional<UserSummaryDto> result = userService.findByDisplayName(displayName);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("닉네임 변경 성공")
    void updateNickname_Success() {
        // Given
        Long userId = 1L;
        String newNickname = "newNickname";
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.existsByDisplayNameAndIdNot(anyString(), eq(userId))).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(user);

        // When
        userService.updateNickname(userId, newNickname);

        // Then
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("닉네임 변경 실패 - 사용자 없음")
    void updateNickname_Fail_UserNotFound() {
        // Given
        Long userId = 999L;
        String newNickname = "newNickname";
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.updateNickname(userId, newNickname))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("닉네임 변경 실패 - DisplayName 중복")
    void updateNickname_Fail_DisplayNameExists() {
        // Given
        Long userId = 1L;
        String newNickname = "existingNickname";
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.existsByDisplayNameAndIdNot(anyString(), eq(userId))).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> userService.updateNickname(userId, newNickname))
                .isInstanceOf(BusinessException.class);

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("닉네임 변경 안함 - 기존 닉네임과 동일")
    void updateNickname_NoChange_SameNickname() {
        // Given
        Long userId = 1L;
        String sameNickname = user.getNickname();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // When
        userService.updateNickname(userId, sameNickname);

        // Then
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("비밀번호 변경 성공")
    void updatePassword_Success() {
        // Given
        Long userId = 1L;
        String currentPassword = "currentPassword123";
        String newPassword = "newPassword123";
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(currentPassword, user.getPassword())).thenReturn(true);
        when(userRepository.save(any(User.class))).thenReturn(user);

        // When
        userService.updatePassword(userId, currentPassword, newPassword);

        // Then
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).save(any(User.class));
        verify(refreshTokenService, times(1)).deleteRefreshToken(user.getEmail());
    }

    @Test
    @DisplayName("비밀번호 변경 실패 - 현재 비밀번호 불일치")
    void updatePassword_Fail_WrongCurrentPassword() {
        // Given
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongPassword", user.getPassword())).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> userService.updatePassword(userId, "wrongPassword", "newPassword123"))
                .isInstanceOf(BusinessException.class);

        verify(userRepository, never()).save(any(User.class));
        verify(refreshTokenService, never()).deleteRefreshToken(any());
    }

    @Test
    @DisplayName("비밀번호 변경 실패 - 빈 비밀번호")
    void updatePassword_Fail_BlankPassword() {
        // Given
        Long userId = 1L;
        String blankPassword = "";

        // When & Then
        assertThatThrownBy(() -> userService.updatePassword(userId, "currentPassword123", blankPassword))
                .isInstanceOf(IllegalArgumentException.class);

        verify(userRepository, never()).findById(userId);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("비밀번호 변경 실패 - null 비밀번호")
    void updatePassword_Fail_NullPassword() {
        // Given
        Long userId = 1L;

        // When & Then
        assertThatThrownBy(() -> userService.updatePassword(userId, "currentPassword123", null))
                .isInstanceOf(IllegalArgumentException.class);

        verify(userRepository, never()).findById(userId);
    }

    @Test
    @DisplayName("비밀번호 변경 실패 - 사용자 없음")
    void updatePassword_Fail_UserNotFound() {
        // Given
        Long userId = 999L;
        String newPassword = "newPassword123";
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.updatePassword(userId, "currentPassword123", newPassword))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("사용자 삭제 성공")
    void deleteUser_Success() {
        // Given
        String email = "test@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        // When
        boolean result = userService.deleteUser(email);

        // Then
        assertThat(result).isTrue();
        assertThat(user.isDeleted()).isTrue();
        assertThat(user.getNickname()).isEqualTo("탈퇴한 사용자");
        assertThat(user.getEmail()).doesNotContain("test@example.com");
        verify(userRepository, times(1)).save(user);
    }

    @Test
    @DisplayName("탈퇴하면 참여 중인 워크스페이스에서 모두 나간다")
    void deleteUser_LeavesWorkspaces() {
        String email = "test@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(workspaceUserRepository.findAllById_UserId(1L)).thenReturn(List.of(
                WorkspaceUser.builder().id(new WorkspaceUserPk(10L, 1L)).build(),
                WorkspaceUser.builder().id(new WorkspaceUserPk(20L, 1L)).build()));

        userService.deleteUser(email);

        verify(workspaceUserService, times(1)).leaveWorkspace(10L, 1L);
        verify(workspaceUserService, times(1)).leaveWorkspace(20L, 1L);
    }

    @Test
    @DisplayName("탈퇴하면 맡고 있던 Task 매니저가 워크스페이스 MASTER 에게 넘어간다")
    void deleteUser_HandsOverTasks() {
        String email = "test@example.com";
        User master = User.builder().id(9L).email("master@test.com").build();
        Workspace workspace = Workspace.builder().id(10L).build();
        Task task = Task.builder().workspace(workspace).taskManager(user).build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(taskRepository.findAllByTaskManager_Id(1L)).thenReturn(List.of(task));
        when(workspaceUserRepository.findFirstById_WorkspaceIdAndRole(10L, WorkspaceRole.MASTER))
                .thenReturn(Optional.of(WorkspaceUser.builder().user(master).build()));

        userService.deleteUser(email);

        assertThat(task.getTaskManager()).isEqualTo(master);
    }

    @Test
    @DisplayName("탈퇴하면 알림과 주고받은 초대를 지운다")
    void deleteUser_ClearsNotificationsAndInvites() {
        String email = "test@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        userService.deleteUser(email);

        verify(notificationRepository, times(1)).deleteByUser_Id(1L);
        verify(inviteRepository, times(1)).deleteByInviteeIdOrInviterId(1L, 1L);
    }

    @Test
    @DisplayName("이미 탈퇴한 계정은 다시 탈퇴할 수 없다")
    void deleteUser_Fail_AlreadyWithdrawn() {
        String email = "test@example.com";
        user.withdraw("withdrawn-1@removed.local", "탈퇴한 사용자#1234-1", "x");
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> userService.deleteUser(email))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("사용자 삭제 실패 - 사용자 없음")
    void deleteUser_Fail_UserNotFound() {
        // Given
        String email = "nonexistent@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.deleteUser(email))
                .isInstanceOf(BusinessException.class);

        verify(userRepository, never()).delete(any(User.class));
    }

    @Test
    @DisplayName("로그인 성공")
    void login_Success() {
        // Given
        String email = "test@example.com";
        String password = "password123";
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(password, user.getPassword())).thenReturn(true);
        when(userMapper.mapToDto(user)).thenReturn(userResponseDto);

        // When
        UserResponseDto result = userService.login(email, password);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo(email);
        verify(userRepository, times(1)).findByEmail(email);
        verify(passwordEncoder, times(1)).matches(password, user.getPassword());
    }

    @Test
    @DisplayName("로그인 실패 - 사용자 없음")
    void login_Fail_UserNotFound() {
        // Given
        String email = "nonexistent@example.com";
        String password = "password123";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.login(email, password))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("로그인 실패 - 비밀번호 불일치")
    void login_Fail_WrongPassword() {
        // Given
        String email = "test@example.com";
        String wrongPassword = "wrongPassword";
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(wrongPassword, user.getPassword())).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> userService.login(email, wrongPassword))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("관리자 생성 성공")
    void createAdmin_Success() {
        // Given
        when(userRepository.findByEmail(userRequestDto.getEmail())).thenReturn(Optional.empty());
        when(userRepository.existsByDisplayName(anyString())).thenReturn(false);
        when(userMapper.mapToEntity(any(UserRequestDto.class), anyString())).thenReturn(user);
        when(userRepository.save(any(User.class))).thenReturn(user);

        // When
        userService.createAdmin(userRequestDto);

        // Then
        verify(userRepository, times(1)).findByEmail(userRequestDto.getEmail());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("관리자 생성 실패 - 이메일 중복")
    void createAdmin_Fail_EmailExists() {
        // Given
        when(userRepository.findByEmail(userRequestDto.getEmail())).thenReturn(Optional.of(user));

        // When & Then
        assertThatThrownBy(() -> userService.createAdmin(userRequestDto))
                .isInstanceOf(BusinessException.class);

        verify(userRepository, never()).save(any(User.class));
    }
}

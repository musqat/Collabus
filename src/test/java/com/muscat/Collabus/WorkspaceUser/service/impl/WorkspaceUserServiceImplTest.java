package com.muscat.Collabus.WorkspaceUser.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.muscat.Collabus.User.entity.User;
import com.muscat.Collabus.Workspace.repository.WorkspaceRepository;
import com.muscat.Collabus.WorkspaceUser.entity.WorkspaceUser;
import com.muscat.Collabus.WorkspaceUser.entity.WorkspaceUserPk;
import com.muscat.Collabus.WorkspaceUser.mapper.WorkspaceUserMapper;
import com.muscat.Collabus.WorkspaceUser.repository.WorkspaceUserRepository;
import com.muscat.Collabus.common.exception.BusinessException;
import com.muscat.Collabus.common.util.ParticipantUtil;
import com.muscat.Collabus.common.util.SortGuard;
import com.muscat.Collabus.common.util.TaskAuthorityUtil;
import com.muscat.Collabus.enums.role.WorkspaceRole;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@DisplayName("워크스페이스 멤버 관리")
class WorkspaceUserServiceImplTest {

  private static final Long WORKSPACE_ID = 10L;
  private static final Long MASTER_ID = 1L;
  private static final Long MEMBER_ID = 2L;

  @Mock
  private TaskAuthorityUtil taskAuthorityUtil;

  @Mock
  private SortGuard sortGuard;

  @Mock
  private WorkspaceUserRepository workspaceUserRepository;

  @Mock
  private WorkspaceUserMapper workspaceUserMapper;

  @Mock
  private ParticipantUtil participantUtil;

  @Mock
  private WorkspaceRepository workspaceRepository;

  @InjectMocks
  private WorkspaceUserServiceImpl workspaceUserService;

  // 참여 시각이 승계 순서를 정하므로 직접 넣는다
  private WorkspaceUser member(Long userId, WorkspaceRole role, int joinOrder) {
    WorkspaceUser member = WorkspaceUser.builder()
        .id(new WorkspaceUserPk(WORKSPACE_ID, userId))
        .user(User.builder().id(userId).build())
        .role(role)
        .build();
    ReflectionTestUtils.setField(member, "createdAt",
        LocalDateTime.of(2026, 1, 1, 0, 0).plusDays(joinOrder));
    return member;
  }

  private void existing(Long userId, WorkspaceUser member) {
    when(workspaceUserRepository.findById(new WorkspaceUserPk(WORKSPACE_ID, userId)))
        .thenReturn(Optional.of(member));
  }

  @Test
  @DisplayName("자기 역할은 바꿀 수 없다")
  void updateUserRole_Fail_Self() {
    assertThatThrownBy(() -> workspaceUserService.updateUserRole(
        WORKSPACE_ID, MASTER_ID, WorkspaceRole.MANAGER, MASTER_ID))
        .isInstanceOf(BusinessException.class);

    verify(workspaceUserRepository, never()).save(any());
  }

  @Test
  @DisplayName("MASTER 로 올리면 기존 MASTER 는 MANAGER 로 내려간다")
  void updateUserRole_PromoteToMaster() {
    WorkspaceUser master = member(MASTER_ID, WorkspaceRole.MASTER, 0);
    WorkspaceUser target = member(MEMBER_ID, WorkspaceRole.MEMBER, 1);
    existing(MEMBER_ID, target);
    existing(MASTER_ID, master);

    workspaceUserService.updateUserRole(
        WORKSPACE_ID, MEMBER_ID, WorkspaceRole.MASTER, MASTER_ID);

    assertThat(master.getRole()).isEqualTo(WorkspaceRole.MANAGER);
    assertThat(target.getRole()).isEqualTo(WorkspaceRole.MASTER);
  }

  @Test
  @DisplayName("MASTER 가 아닌 역할로 바꿀 때는 기존 MASTER 를 건드리지 않는다")
  void updateUserRole_ToManager() {
    WorkspaceUser target = member(MEMBER_ID, WorkspaceRole.MEMBER, 1);
    existing(MEMBER_ID, target);

    workspaceUserService.updateUserRole(
        WORKSPACE_ID, MEMBER_ID, WorkspaceRole.MANAGER, MASTER_ID);

    assertThat(target.getRole()).isEqualTo(WorkspaceRole.MANAGER);
    verify(workspaceUserRepository, times(1)).save(target);
  }

  @Test
  @DisplayName("MASTER 가 아니면 역할을 바꿀 수 없다")
  void updateUserRole_Fail_NotMaster() {
    doThrow(new BusinessException(
        com.muscat.Collabus.enums.response.CommonResponse.WORKSPACE_MASTER_REQUIRED))
        .when(taskAuthorityUtil).validateWorkspaceMaster(WORKSPACE_ID, MEMBER_ID);

    assertThatThrownBy(() -> workspaceUserService.updateUserRole(
        WORKSPACE_ID, MASTER_ID, WorkspaceRole.MANAGER, MEMBER_ID))
        .isInstanceOf(BusinessException.class);
  }

  @Test
  @DisplayName("자기 자신은 제거할 수 없다")
  void removeUser_Fail_Self() {
    assertThatThrownBy(() ->
        workspaceUserService.removeUser(WORKSPACE_ID, MASTER_ID, MASTER_ID))
        .isInstanceOf(BusinessException.class);

    verify(workspaceUserRepository, never()).delete(any());
  }

  @Test
  @DisplayName("MASTER 는 다른 멤버를 제거할 수 있다")
  void removeUser_Success() {
    WorkspaceUser target = member(MEMBER_ID, WorkspaceRole.MEMBER, 1);
    existing(MEMBER_ID, target);

    workspaceUserService.removeUser(WORKSPACE_ID, MEMBER_ID, MASTER_ID);

    verify(workspaceUserRepository, times(1)).delete(target);
  }

  @Test
  @DisplayName("마지막 멤버가 나가면 워크스페이스까지 지운다")
  void leaveWorkspace_LastMember() {
    WorkspaceUser only = member(MASTER_ID, WorkspaceRole.MASTER, 0);
    existing(MASTER_ID, only);
    when(workspaceUserRepository.findAllById_WorkspaceId(WORKSPACE_ID))
        .thenReturn(List.of(only));

    workspaceUserService.leaveWorkspace(WORKSPACE_ID, MASTER_ID);

    verify(workspaceUserRepository, times(1)).delete(only);
    verify(workspaceRepository, times(1)).deleteById(WORKSPACE_ID);
  }

  @Test
  @DisplayName("MASTER 가 나가면 MANAGER 가 먼저 승계한다")
  void leaveWorkspace_ManagerSucceeds() {
    WorkspaceUser master = member(MASTER_ID, WorkspaceRole.MASTER, 0);
    WorkspaceUser member = member(MEMBER_ID, WorkspaceRole.MEMBER, 1);
    WorkspaceUser manager = member(3L, WorkspaceRole.MANAGER, 2);
    existing(MASTER_ID, master);
    when(workspaceUserRepository.findAllById_WorkspaceId(WORKSPACE_ID))
        .thenReturn(List.of(master, member, manager));

    workspaceUserService.leaveWorkspace(WORKSPACE_ID, MASTER_ID);

    assertThat(manager.getRole()).isEqualTo(WorkspaceRole.MASTER);
    assertThat(member.getRole()).isEqualTo(WorkspaceRole.MEMBER);
  }

  @Test
  @DisplayName("전부 MEMBER 면 먼저 참여한 사람이 승계한다")
  void leaveWorkspace_OldestMemberSucceeds() {
    WorkspaceUser master = member(MASTER_ID, WorkspaceRole.MASTER, 0);
    WorkspaceUser later = member(3L, WorkspaceRole.MEMBER, 5);
    WorkspaceUser earlier = member(MEMBER_ID, WorkspaceRole.MEMBER, 1);
    existing(MASTER_ID, master);
    when(workspaceUserRepository.findAllById_WorkspaceId(WORKSPACE_ID))
        .thenReturn(List.of(master, later, earlier));

    workspaceUserService.leaveWorkspace(WORKSPACE_ID, MASTER_ID);

    assertThat(earlier.getRole()).isEqualTo(WorkspaceRole.MASTER);
    assertThat(later.getRole()).isEqualTo(WorkspaceRole.MEMBER);
  }

  @Test
  @DisplayName("MASTER 가 아닌 사람이 나가면 승계가 일어나지 않는다")
  void leaveWorkspace_NoSuccession() {
    WorkspaceUser master = member(MASTER_ID, WorkspaceRole.MASTER, 0);
    WorkspaceUser leaving = member(MEMBER_ID, WorkspaceRole.MEMBER, 1);
    existing(MEMBER_ID, leaving);
    when(workspaceUserRepository.findAllById_WorkspaceId(WORKSPACE_ID))
        .thenReturn(List.of(master, leaving));

    workspaceUserService.leaveWorkspace(WORKSPACE_ID, MEMBER_ID);

    assertThat(master.getRole()).isEqualTo(WorkspaceRole.MASTER);
    verify(workspaceUserRepository, times(1)).delete(leaving);
    verify(workspaceRepository, never()).deleteById(any());
  }
}

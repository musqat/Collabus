package com.muscat.Collabus.WorkspaceUser.service.impl;

import static org.mockito.Mockito.times;
import static org.mockito.ArgumentMatchers.any;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.muscat.Collabus.common.util.TaskAuthorityUtil;
import java.util.Optional;
import com.muscat.Collabus.enums.role.WorkspaceRole;
import com.muscat.Collabus.common.exception.BusinessException;
import com.muscat.Collabus.WorkspaceUser.model.InviteRequestDto;
import com.muscat.Collabus.WorkspaceUser.entity.WorkspaceUserPk;
import com.muscat.Collabus.common.util.SortGuard;
import com.muscat.Collabus.Notification.service.NotificationService;
import com.muscat.Collabus.User.repository.UserRepository;
import com.muscat.Collabus.Workspace.repository.WorkspaceRepository;
import com.muscat.Collabus.WorkspaceUser.entity.WorkspaceInvite;
import com.muscat.Collabus.WorkspaceUser.mapper.WorkspaceInviteMapper;
import com.muscat.Collabus.WorkspaceUser.model.InviteResponseDto;
import com.muscat.Collabus.WorkspaceUser.repository.WorkspaceInviteRepository;
import com.muscat.Collabus.WorkspaceUser.repository.WorkspaceUserRepository;
import com.muscat.Collabus.common.dto.PageResponseDto;
import com.muscat.Collabus.enums.status.InviteStatus;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
@DisplayName("WorkspaceUserInviteService 단위 테스트")
class WorkspaceUserInviteServiceImplTest {

  @Mock

  private TaskAuthorityUtil taskAuthorityUtil;


  @Mock

  private SortGuard sortGuard;


  @Mock
  private WorkspaceRepository workspaceRepository;

  @Mock
  private UserRepository userRepository;

  @Mock
  private WorkspaceInviteRepository inviteRepository;

  @Mock
  private WorkspaceUserRepository workspaceUserRepository;

  @Mock
  private WorkspaceInviteMapper inviteMapper;

  @Mock
  private NotificationService notificationService;

  @InjectMocks
  private WorkspaceUserInviteServiceImpl inviteService;

  @Test
  @DisplayName("받은 초대는 대기 중인 것만, 요청한 페이지 크기만큼 돌려준다")
  void getMyInvites_PendingOnly_Paged() {
    Pageable pageable = PageRequest.of(0, 20);
    // 정렬 검증은 SortGuard 가 맡는다. 여기서는 그대로 통과시킨다
    when(sortGuard.apply(pageable, WorkspaceInvite.class)).thenReturn(pageable);
    WorkspaceInvite invite = WorkspaceInvite.builder().build();
    InviteResponseDto dto = InviteResponseDto.builder().build();

    // 전체 45건 중 첫 페이지 20건
    when(inviteRepository.findAllByInviteeIdAndStatus(1L, InviteStatus.PENDING, pageable))
        .thenReturn(new PageImpl<>(List.of(invite), pageable, 45));
    when(inviteMapper.mapToDto(invite)).thenReturn(dto);

    PageResponseDto<InviteResponseDto> result = inviteService.getMyInvites(1L, pageable);

    assertThat(result.getContent()).containsExactly(dto);
    assertThat(result.getTotalElements()).isEqualTo(45);
    assertThat(result.getTotalPages()).isEqualTo(3);
    assertThat(result.isHasNext()).isTrue();
  }

  @Test
  @DisplayName("페이지 요청이 리포지토리까지 그대로 전달된다")
  void getMyInvites_PassesPageable() {
    Pageable pageable = PageRequest.of(2, 5);
    // 정렬 검증은 SortGuard 가 맡는다. 여기서는 그대로 통과시킨다
    when(sortGuard.apply(pageable, WorkspaceInvite.class)).thenReturn(pageable);
    when(inviteRepository.findAllByInviteeIdAndStatus(1L, InviteStatus.PENDING, pageable))
        .thenReturn(new PageImpl<>(List.of(), pageable, 0));

    inviteService.getMyInvites(1L, pageable);

    ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
    verify(inviteRepository)
        .findAllByInviteeIdAndStatus(eq(1L), eq(InviteStatus.PENDING), captor.capture());
    assertThat(captor.getValue().getPageNumber()).isEqualTo(2);
    assertThat(captor.getValue().getPageSize()).isEqualTo(5);
  }

  @Test
  @DisplayName("자기 자신은 초대할 수 없다")
  void invite_Fail_Self() {
    InviteRequestDto dto = new InviteRequestDto(1L, WorkspaceRole.MEMBER);

    assertThatThrownBy(() -> inviteService.inviteUserToWorkspace(1L, 10L, dto))
        .isInstanceOf(BusinessException.class);

    verify(inviteRepository, times(0)).save(any());
  }

  @Test
  @DisplayName("이미 멤버면 초대할 수 없다")
  void invite_Fail_AlreadyMember() {
    InviteRequestDto dto = new InviteRequestDto(2L, WorkspaceRole.MEMBER);
    when(workspaceUserRepository.existsById(new WorkspaceUserPk(10L, 2L))).thenReturn(true);

    assertThatThrownBy(() -> inviteService.inviteUserToWorkspace(1L, 10L, dto))
        .isInstanceOf(BusinessException.class);

    verify(inviteRepository, times(0)).save(any());
  }

  @Test
  @DisplayName("대기 중 초대가 있으면 다시 초대할 수 없다")
  void invite_Fail_AlreadyPending() {
    InviteRequestDto dto = new InviteRequestDto(2L, WorkspaceRole.MEMBER);
    when(workspaceUserRepository.existsById(new WorkspaceUserPk(10L, 2L))).thenReturn(false);
    when(inviteRepository.existsByWorkspaceIdAndInviteeIdAndStatus(10L, 2L, InviteStatus.PENDING))
        .thenReturn(true);

    assertThatThrownBy(() -> inviteService.inviteUserToWorkspace(1L, 10L, dto))
        .isInstanceOf(BusinessException.class);
  }

  @Test
  @DisplayName("이미 처리된 초대는 다시 수락할 수 없다")
  void acceptInvite_Fail_AlreadyProcessed() {
    WorkspaceInvite invite = WorkspaceInvite.builder().status(InviteStatus.ACCEPTED).build();
    when(inviteRepository.findByIdAndInviteeId(1L, 2L)).thenReturn(Optional.of(invite));

    assertThatThrownBy(() -> inviteService.acceptInvite(1L, 2L))
        .isInstanceOf(BusinessException.class);

    verify(workspaceUserRepository, times(0)).save(any());
  }

  @Test
  @DisplayName("본인에게 온 초대가 아니면 수락할 수 없다")
  void acceptInvite_Fail_NotMine() {
    when(inviteRepository.findByIdAndInviteeId(1L, 99L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> inviteService.acceptInvite(1L, 99L))
        .isInstanceOf(BusinessException.class);
  }
}

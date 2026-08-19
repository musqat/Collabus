package com.muscat.Collabus.Workspace.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.PageImpl;
import com.muscat.Collabus.common.util.SortGuard;
import com.muscat.Collabus.common.dto.PageResponseDto;
import com.muscat.Collabus.User.entity.User;
import com.muscat.Collabus.Workspace.entity.Workspace;
import com.muscat.Collabus.Workspace.mapper.WorkspaceMapper;
import com.muscat.Collabus.Workspace.model.WorkspaceRequestDto;
import com.muscat.Collabus.Workspace.model.WorkspaceResponseDto;
import com.muscat.Collabus.Task.repository.TaskRepository;
import com.muscat.Collabus.Task.repository.TaskUserRepository;
import com.muscat.Collabus.Todo.repository.TodoCommentRepository;
import com.muscat.Collabus.Todo.repository.TodoFileRepository;
import com.muscat.Collabus.Todo.repository.TodoRepository;
import com.muscat.Collabus.Todo.repository.TodoWorkRepository;
import com.muscat.Collabus.Workspace.repository.WorkspaceRepository;
import com.muscat.Collabus.WorkspaceUser.entity.WorkspaceUser;
import com.muscat.Collabus.WorkspaceUser.repository.WorkspaceUserRepository;
import com.muscat.Collabus.common.exception.BusinessException;
import com.muscat.Collabus.enums.response.CommonResponse;
import com.muscat.Collabus.common.exception.ResourceNotFoundException;
import com.muscat.Collabus.common.util.EntityFinderUtil;
import com.muscat.Collabus.common.util.TaskAuthorityUtil;
import com.muscat.Collabus.enums.role.SystemRole;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.context.ApplicationEventPublisher;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("WorkspaceService 단위 테스트")
class WorkspaceServiceImplTest {

    @Mock
    private SortGuard sortGuard;


    @Mock
    private WorkspaceRepository workspaceRepository;

    @Mock
    private WorkspaceMapper workspaceMapper;

    @Mock
    private WorkspaceUserRepository workspaceUserRepository;

    @Mock
    private EntityFinderUtil entityFinderUtil;

    @Mock
    private TaskAuthorityUtil taskAuthorityUtil;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private TaskUserRepository taskUserRepository;

    @Mock
    private TodoRepository todoRepository;

    @Mock
    private TodoWorkRepository todoWorkRepository;

    @Mock
    private TodoCommentRepository todoCommentRepository;

    @Mock
    private TodoFileRepository todoFileRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private WorkspaceServiceImpl workspaceService;

    private User founder;
    private Workspace workspace;
    private WorkspaceRequestDto requestDto;
    private WorkspaceResponseDto responseDto;
    private WorkspaceUser workspaceUser;

    @BeforeEach
    void setUp() {
        founder = User.builder()
                .id(1L)
                .email("founder@example.com")
                .nickname("founder")
                .password("encodedPassword")
                .tag("1234")
                .displayName("founder#1234")
                .role(SystemRole.USER)
                .build();

        workspace = Workspace.builder()
                .id(1L)
                .workspaceName("Test Workspace")
                .description("Test Description")
                .founder(founder)
                .build();

        requestDto = WorkspaceRequestDto.builder()
                .workspaceName("Test Workspace")
                .description("Test Description")
                .build();

        responseDto = WorkspaceResponseDto.builder()
                .id(1L)
                .workspaceName("Test Workspace")
                .description("Test Description")
                .founderDisplayName("founder#1234")
                .build();
    }

    @Test
    @DisplayName("워크스페이스 생성 성공")
    void createWorkspace_Success() {
        // Given
        Long founderId = 1L;
        when(entityFinderUtil.findUserById(founderId)).thenReturn(founder);
        // 서비스가 빌더로 새 Workspace 인스턴스를 만들므로 setUp 의 인스턴스로는 매칭되지 않는다
        when(workspaceRepository.save(any(Workspace.class))).thenReturn(workspace);
        when(workspaceUserRepository.save(any(WorkspaceUser.class))).thenReturn(workspaceUser);
        when(workspaceMapper.mapToDto(any(Workspace.class))).thenReturn(responseDto);

        // When
        WorkspaceResponseDto result = workspaceService.createWorkspace(requestDto, founderId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getWorkspaceName()).isEqualTo("Test Workspace");
        verify(entityFinderUtil, times(1)).findUserById(founderId);
        verify(workspaceRepository, times(1)).save(any(Workspace.class));
        verify(workspaceUserRepository, times(1)).save(any(WorkspaceUser.class));
    }

    @Test
    @DisplayName("워크스페이스 생성 실패 - 사용자 없음")
    void createWorkspace_Fail_UserNotFound() {
        // Given
        Long founderId = 999L;
        when(entityFinderUtil.findUserById(founderId))
                .thenThrow(new ResourceNotFoundException(CommonResponse.RESOURCE_NOT_FOUND));

        // When & Then
        assertThatThrownBy(() -> workspaceService.createWorkspace(requestDto, founderId))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(workspaceRepository, times(0)).save(any(Workspace.class));
    }

    @Test
    @DisplayName("워크스페이스 ID로 조회 성공")
    void getWorkspaceById_Success() {
        // Given
        Long workspaceId = 1L;
        when(entityFinderUtil.findWorkspaceById(workspaceId)).thenReturn(workspace);
        when(workspaceMapper.mapToDto(workspace)).thenReturn(responseDto);

        // When
        WorkspaceResponseDto result = workspaceService.getWorkspaceById(workspaceId, 1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(workspaceId);
        assertThat(result.getWorkspaceName()).isEqualTo("Test Workspace");
        verify(entityFinderUtil, times(1)).findWorkspaceById(workspaceId);
    }

    @Test
    @DisplayName("워크스페이스 조회 실패 - 존재하지 않음")
    void getWorkspaceById_Fail_NotFound() {
        // Given
        Long workspaceId = 999L;
        when(entityFinderUtil.findWorkspaceById(workspaceId))
                .thenThrow(new ResourceNotFoundException(CommonResponse.RESOURCE_NOT_FOUND));

        // When & Then
        assertThatThrownBy(() -> workspaceService.getWorkspaceById(workspaceId, 1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("내가 만든 워크스페이스 목록 조회 성공")
    void getMyWorkspaces_Success() {
        // Given
        Long userId = 1L;
        Pageable pageable = PageRequest.of(0, 20);
        when(sortGuard.apply(pageable, Workspace.class)).thenReturn(pageable);
        when(workspaceRepository.findAllByFounderId(userId, pageable))
                .thenReturn(new PageImpl<>(List.of(workspace), pageable, 1));
        when(workspaceMapper.mapToDto(workspace)).thenReturn(responseDto);

        // When
        PageResponseDto<WorkspaceResponseDto> result =
                workspaceService.getMyWorkspaces(userId, pageable);

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getWorkspaceName()).isEqualTo("Test Workspace");
    }

    @Test
    @DisplayName("내가 만든 워크스페이스 목록 조회 - 빈 목록")
    void getMyWorkspaces_EmptyList() {
        // Given
        Long userId = 1L;
        Pageable pageable = PageRequest.of(0, 20);
        when(sortGuard.apply(pageable, Workspace.class)).thenReturn(pageable);
        when(workspaceRepository.findAllByFounderId(userId, pageable))
                .thenReturn(new PageImpl<>(List.of(), pageable, 0));

        // When
        PageResponseDto<WorkspaceResponseDto> result =
                workspaceService.getMyWorkspaces(userId, pageable);

        // Then
        assertThat(result.getContent()).isEmpty();
    }

    @Test
    @DisplayName("워크스페이스 수정 성공")
    void updateWorkspace_Success() {
        // Given
        Long workspaceId = 1L;
        Long userId = 1L;
        WorkspaceRequestDto updateDto = WorkspaceRequestDto.builder()
                .workspaceName("Updated Workspace")
                .description("Updated Description")
                .build();

        when(entityFinderUtil.findWorkspaceById(workspaceId)).thenReturn(workspace);
        when(workspaceRepository.save(workspace)).thenReturn(workspace);
        when(workspaceMapper.mapToDto(workspace)).thenReturn(responseDto);

        // When
        WorkspaceResponseDto result = workspaceService.updateWorkspace(workspaceId, updateDto, userId);

        // Then
        assertThat(result).isNotNull();
        verify(entityFinderUtil, times(1)).findWorkspaceById(workspaceId);
        verify(taskAuthorityUtil, times(1)).validateWorkspaceMaster(workspace, userId);
        verify(workspaceMapper, times(1)).updateEntity(workspace, updateDto);
        verify(workspaceRepository, times(1)).save(workspace);
    }

    @Test
    @DisplayName("워크스페이스 수정 실패 - 권한 없음")
    void updateWorkspace_Fail_Unauthorized() {
        // Given
        Long workspaceId = 1L;
        Long userId = 2L; // 다른 사용자
        WorkspaceRequestDto updateDto = WorkspaceRequestDto.builder()
                .workspaceName("Updated Workspace")
                .description("Updated Description")
                .build();

        when(entityFinderUtil.findWorkspaceById(workspaceId)).thenReturn(workspace);
        doThrow(new BusinessException(CommonResponse.FORBIDDEN))
                .when(taskAuthorityUtil).validateWorkspaceMaster(workspace, userId);

        // When & Then
        assertThatThrownBy(() -> workspaceService.updateWorkspace(workspaceId, updateDto, userId))
                .isInstanceOf(BusinessException.class);

        verify(workspaceRepository, times(0)).save(any(Workspace.class));
    }

    @Test
    @DisplayName("워크스페이스 수정 실패 - 워크스페이스 없음")
    void updateWorkspace_Fail_WorkspaceNotFound() {
        // Given
        Long workspaceId = 999L;
        Long userId = 1L;
        WorkspaceRequestDto updateDto = WorkspaceRequestDto.builder()
                .workspaceName("Updated Workspace")
                .description("Updated Description")
                .build();

        when(entityFinderUtil.findWorkspaceById(workspaceId))
                .thenThrow(new ResourceNotFoundException(CommonResponse.RESOURCE_NOT_FOUND));

        // When & Then
        assertThatThrownBy(() -> workspaceService.updateWorkspace(workspaceId, updateDto, userId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("워크스페이스 삭제 성공")
    void deleteWorkspace_Success() {
        // Given
        Long workspaceId = 1L;
        Long userId = 1L;
        when(entityFinderUtil.findWorkspaceById(workspaceId)).thenReturn(workspace);

        // When
        workspaceService.deleteWorkspace(workspaceId, userId);

        // Then
        verify(entityFinderUtil, times(1)).findWorkspaceById(workspaceId);
        verify(taskAuthorityUtil, times(1)).validateWorkspaceMaster(workspace, userId);
        verify(workspaceRepository, times(1)).delete(workspace);
    }

    @Test
    @DisplayName("워크스페이스 삭제 실패 - 권한 없음")
    void deleteWorkspace_Fail_Unauthorized() {
        // Given
        Long workspaceId = 1L;
        Long userId = 2L; // 다른 사용자
        when(entityFinderUtil.findWorkspaceById(workspaceId)).thenReturn(workspace);
        doThrow(new BusinessException(CommonResponse.FORBIDDEN))
                .when(taskAuthorityUtil).validateWorkspaceMaster(workspace, userId);

        // When & Then
        assertThatThrownBy(() -> workspaceService.deleteWorkspace(workspaceId, userId))
                .isInstanceOf(BusinessException.class);

        verify(workspaceRepository, times(0)).delete(any(Workspace.class));
    }

    @Test
    @DisplayName("워크스페이스 삭제 실패 - 워크스페이스 없음")
    void deleteWorkspace_Fail_WorkspaceNotFound() {
        // Given
        Long workspaceId = 999L;
        Long userId = 1L;
        when(entityFinderUtil.findWorkspaceById(workspaceId))
                .thenThrow(new ResourceNotFoundException(CommonResponse.RESOURCE_NOT_FOUND));

        // When & Then
        assertThatThrownBy(() -> workspaceService.deleteWorkspace(workspaceId, userId))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(workspaceRepository, times(0)).delete(any(Workspace.class));
    }
}

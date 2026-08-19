package com.muscat.Collabus.Workspace.service;

import com.muscat.Collabus.Workspace.model.WorkspaceRequestDto;
import com.muscat.Collabus.common.dto.PageResponseDto;
import org.springframework.data.domain.Pageable;
import com.muscat.Collabus.Workspace.model.WorkspaceResponseDto;
public interface WorkspaceService {

  // workspace 생성
  WorkspaceResponseDto createWorkspace(WorkspaceRequestDto dto, Long founderId);

  // workspace 단일 조회
  WorkspaceResponseDto getWorkspaceById(Long workspaceId);

  // 내가 속한 workspace 전체 조회
  PageResponseDto<WorkspaceResponseDto> getMyWorkspaces(Long userId, Pageable pageable);

  // 내가 참여 중인 workspace 전체 조회 (founder가 아닌 경우 포함)
  PageResponseDto<WorkspaceResponseDto> getJoinedWorkspaces(Long userId, Pageable pageable);

  // workspace 수정
  WorkspaceResponseDto updateWorkspace(Long id, WorkspaceRequestDto dto, Long userId);

  // workspace 삭제
  void deleteWorkspace(Long workspaceId, Long userId);
}


package com.muscat.Collabus.Workspace.service;

import com.muscat.Collabus.Workspace.model.WorkspaceRequestDto;
import com.muscat.Collabus.Workspace.model.WorkspaceResponseDto;
import java.util.List;

public interface WorkspaceService {

  /**
   * workspace 생성
   */
  WorkspaceResponseDto createWorkspace(WorkspaceRequestDto dto, Long founderId);

  /**
   * workspace 단일 조회
   */
  WorkspaceResponseDto getWorkspaceById(Long workspaceId);

  /**
   * 내가 속한 workspace 전체 조회
   */
  List<WorkspaceResponseDto> getMyWorkspaces(Long userId);

  /**
   * workspace 수정
   */
  WorkspaceResponseDto updateWorkspace(Long id, WorkspaceRequestDto dto, Long userId);

  /**
   * workspace 삭제
   */
  void deleteWorkspace(Long workspaceId, Long userId);
}


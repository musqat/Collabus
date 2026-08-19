package com.muscat.Collabus.Workspace.service;

import com.muscat.Collabus.Workspace.model.WorkspaceRequestDto;
import com.muscat.Collabus.common.dto.PageResponseDto;
import org.springframework.data.domain.Pageable;
import com.muscat.Collabus.Workspace.model.WorkspaceResponseDto;
public interface WorkspaceService {

  // 워크스페이스를 만든다. 생성자가 MASTER 로 등록된다
  WorkspaceResponseDto createWorkspace(WorkspaceRequestDto dto, Long founderId);

  // 워크스페이스 단건 조회
  WorkspaceResponseDto getWorkspaceById(Long workspaceId, Long requesterId);

  // 내가 만든 워크스페이스 목록
  PageResponseDto<WorkspaceResponseDto> getMyWorkspaces(Long userId, Pageable pageable);

  // 참여 중인 워크스페이스 목록. 내가 만든 것도 포함된다
  PageResponseDto<WorkspaceResponseDto> getJoinedWorkspaces(Long userId, Pageable pageable);

  // 워크스페이스를 수정한다
  WorkspaceResponseDto updateWorkspace(Long id, WorkspaceRequestDto dto, Long userId);

  // 워크스페이스를 삭제한다. 하위 Task·Todo 와 첨부 파일까지 지운다
  void deleteWorkspace(Long workspaceId, Long userId);
}


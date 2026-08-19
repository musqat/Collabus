package com.muscat.Collabus.Task.service;

import com.muscat.Collabus.common.dto.PageResponseDto;
import org.springframework.data.domain.Pageable;
import com.muscat.Collabus.Task.model.TaskRequestDto;
import com.muscat.Collabus.Task.model.TaskResponseDto;
import com.muscat.Collabus.Task.model.TaskUpdateRequestDto;
import com.muscat.Collabus.Task.model.TaskUserResponseDto;
import com.muscat.Collabus.Task.model.TodoProgressDto;


public interface TaskService {

    // 워크스페이스 MASTER·MANAGER 만 생성 가능
    TaskResponseDto createTask(TaskRequestDto dto, Long creatorId);

    // 참여자이거나 워크스페이스 MASTER·MANAGER 만 조회 가능
    TaskResponseDto getTask(Long taskId, Long requesterId);

    // 참여자만 조회 가능. MEMBER 는 자신이 속한 Task 만 보이고, keyword 는 제목·설명에 걸린다.
    PageResponseDto<TaskResponseDto> getTasksByWorkspace(Long workspaceId, Long requesterId,
        String keyword, Pageable pageable);

    // 볼 수 있는 Task 의 Todo 를 상태별로 센다
    TodoProgressDto getWorkspaceProgress(Long workspaceId, Long requesterId);

    // Task 의 Todo 를 상태별로 센다
    TodoProgressDto getTaskProgress(Long taskId, Long requesterId);

    // 워크스페이스 MASTER 또는 Task Manager 만 수정 가능
    TaskResponseDto updateTask(Long taskId, TaskUpdateRequestDto dto, Long userId);

    // 워크스페이스 MASTER 또는 Task Manager 만 삭제 가능. 하위 Todo 와 첨부 파일까지 정리된다
    void deleteTask(Long taskId, Long userId);

    // 워크스페이스 MASTER 만 추가 가능. 추가된 사용자에게 알림이 간다
    void assignUserToTask(Long taskId, Long targetUserId, Long requesterId);

    // 워크스페이스 MASTER 만 제거 가능. Task Manager 는 자기 자신을 뺄 수 없다
    void removeUserFromTask(Long taskId, Long targetUserId, Long requesterId);

    // Task 참여자 목록
    PageResponseDto<TaskUserResponseDto> getTaskMembers(Long taskId, Long requesterId,
        Pageable pageable);

    // 매니저 이전. 기존 매니저는 NORMAL 로 강등된다
    void assignTaskManager(Long taskId, Long newManagerId, Long requesterId);

}

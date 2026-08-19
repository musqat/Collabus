package com.muscat.Collabus.Task.service;

import com.muscat.Collabus.common.dto.PageResponseDto;
import org.springframework.data.domain.Pageable;
import com.muscat.Collabus.Task.model.TaskRequestDto;
import com.muscat.Collabus.Task.model.TaskResponseDto;
import com.muscat.Collabus.Task.model.TaskUpdateRequestDto;
import com.muscat.Collabus.Task.model.TaskUserResponseDto;
import com.muscat.Collabus.Task.model.TodoProgressDto;


public interface TaskService {

    // Task 를 만든다
    TaskResponseDto createTask(TaskRequestDto dto, Long creatorId);

    // Task 단건 조회
    TaskResponseDto getTask(Long taskId, Long requesterId);

    // 워크스페이스의 Task 목록. keyword 는 제목·설명에 걸린다
    PageResponseDto<TaskResponseDto> getTasksByWorkspace(Long workspaceId, Long requesterId,
        String keyword, Pageable pageable);

    // 워크스페이스의 Todo 를 상태별로 센다
    TodoProgressDto getWorkspaceProgress(Long workspaceId, Long requesterId);

    // Task 의 Todo 를 상태별로 센다
    TodoProgressDto getTaskProgress(Long taskId, Long requesterId);

    // Task 를 수정한다
    TaskResponseDto updateTask(Long taskId, TaskUpdateRequestDto dto, Long userId);

    // Task 를 삭제한다. 하위 Todo 와 첨부 파일까지 지운다
    void deleteTask(Long taskId, Long userId);

    // Task 에 참여자를 추가한다
    void assignUserToTask(Long taskId, Long targetUserId, Long requesterId);

    // Task 에서 참여자를 제거한다
    void removeUserFromTask(Long taskId, Long targetUserId, Long requesterId);

    // Task 참여자 목록
    PageResponseDto<TaskUserResponseDto> getTaskMembers(Long taskId, Long requesterId,
        Pageable pageable);

    // Task Manager 를 넘긴다. 기존 매니저는 NORMAL 로 내려간다
    void assignTaskManager(Long taskId, Long newManagerId, Long requesterId);

}

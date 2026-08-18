package com.muscat.Collabus.Task.service;

import com.muscat.Collabus.common.dto.PageResponseDto;
import org.springframework.data.domain.Pageable;
import com.muscat.Collabus.Task.model.TaskRequestDto;
import com.muscat.Collabus.Task.model.TaskResponseDto;
import com.muscat.Collabus.Task.model.TaskUpdateRequestDto;
import com.muscat.Collabus.Task.model.TaskUserResponseDto;

import java.util.List;

public interface TaskService {

    // CRUD
    TaskResponseDto createTask(TaskRequestDto dto, Long creatorId);

    TaskResponseDto getTask(Long taskId); // Task 단건 조회

    PageResponseDto<TaskResponseDto> getTasksByWorkspace(Long workspaceId, Pageable pageable);

    TaskResponseDto updateTask(Long taskId, TaskUpdateRequestDto dto, Long userId);

    void deleteTask(Long taskId);

    // Task 유저 관리
    void assignUserToTask(Long taskId, Long targetUserId, Long requesterId); // 워크스페이스 멤버만 가능

    void removeUserFromTask(Long taskId, Long targetUserId, Long requesterId); // 강퇴 or 탈퇴

    List<TaskUserResponseDto> getTaskMembers(Long taskId); // 멤버 조회

    // TaskManager 관련
    void assignTaskManager(Long taskId, Long newManagerId, Long requesterId); //  MANAGER 설정

}

package com.muscat.Collabus.Todo.repository;

import com.muscat.Collabus.Todo.entity.TodoFile;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TodoFileRepository extends JpaRepository<TodoFile, Long> {

    // 목록 응답에 업로더 정보가 필요하므로 함께 가져온다
    @EntityGraph(attributePaths = {"uploader"})
    List<TodoFile> findAllByWorkId(Long workId);

    // 삭제 대상 파일의 저장 경로만 뽑는다
    interface FileLocation {

        String getFileUrl();
    }

    // 워크스페이스 삭제 시 정리할 파일 (workspace > task > todo > work > file)
    List<FileLocation> findAllByWork_Todo_Task_Workspace_Id(Long workspaceId);

    // Task 삭제 시 정리할 파일
    List<FileLocation> findAllByWork_Todo_Task_Id(Long taskId);

    // Todo 삭제 시 정리할 파일
    List<FileLocation> findAllByWork_Todo_Id(Long todoId);
}

package com.muscat.Collabus.Task.repository;

import com.muscat.Collabus.Task.entity.Task;
import com.muscat.Collabus.Workspace.entity.Workspace;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<Task, Long> {
  //특정 워크스페이스에 속한 모든 Task 조회.
  List<Task> findAllByWorkspace(Workspace workspace);

  //특정 사용자 ID로 Task 조회
  List<Task> findAllByWorkspace_Id(Long workspaceId);

}

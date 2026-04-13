package com.muscat.Collabus.Task.repository;

import com.muscat.Collabus.Task.entity.Task;
import com.muscat.Collabus.Task.entity.TaskUser;
import com.muscat.Collabus.Task.entity.TaskUserPk;
import com.muscat.Collabus.User.entity.User;
import com.muscat.Collabus.enums.role.TaskRole;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TaskUserRepository extends JpaRepository<TaskUser, TaskUserPk> {

  List<TaskUser> findAllByTask(Task task);

  Optional<TaskUser> findByTaskAndUser(Task task, User user);

  boolean existsByTaskAndUser(Task task, User user);

  void deleteAllByTask(Task task);

  List<TaskUser> findByTaskAndRole(Task task, TaskRole role);

  @Modifying
  @Query("DELETE FROM TaskUser tu WHERE tu.task.workspace.id = :workspaceId")
  void deleteAllByWorkspaceId(@Param("workspaceId") Long workspaceId);
}

package com.muscat.Collabus.Task.repository;

import com.muscat.Collabus.Task.entity.Task;
import com.muscat.Collabus.Task.entity.TaskUser;
import com.muscat.Collabus.Task.entity.TaskUserPk;
import com.muscat.Collabus.User.entity.User;
import com.muscat.Collabus.enums.role.TaskRole;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskUserRepository extends JpaRepository<TaskUser, TaskUserPk> {

  @EntityGraph(attributePaths = {"user"})
  List<TaskUser> findAllByTask(Task task);

  Optional<TaskUser> findByTaskAndUser(Task task, User user);

  boolean existsByTaskAndUser(Task task, User user);

  List<TaskUser> findByTaskAndRole(Task task, TaskRole role);
}

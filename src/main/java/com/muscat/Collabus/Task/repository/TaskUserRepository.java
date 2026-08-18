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

  // Task 참여자 전원. 알림 대상 선정에 쓰이므로 페이징하지 않고 사용자까지 함께 가져온다
  @EntityGraph(attributePaths = {"user"})
  List<TaskUser> findAllByTask(Task task);

  // 특정 사용자의 Task 참여 정보 (역할 변경·제거에 사용)
  Optional<TaskUser> findByTaskAndUser(Task task, User user);

  // Task 참여자인지 확인
  boolean existsByTaskAndUser(Task task, User user);

  // 역할별 참여자. 검수 요청 알림을 MANAGER 전원에게 보낼 때 쓴다
  List<TaskUser> findByTaskAndRole(Task task, TaskRole role);
}

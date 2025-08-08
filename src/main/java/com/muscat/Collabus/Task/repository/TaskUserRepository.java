package com.muscat.Collabus.Task.repository;

import com.muscat.Collabus.Task.entity.Task;
import com.muscat.Collabus.Task.entity.TaskUser;
import com.muscat.Collabus.Task.entity.TaskUserPk;
import com.muscat.Collabus.User.entity.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskUserRepository extends JpaRepository<TaskUser, TaskUserPk> {

  //  특정 Task에 참여 중인 모든 유저 조회
  List<TaskUser> findAllByTask(Task task); // 가독성 위해 findBy → findAllBy

  //  특정 Task와 User에 대한 참여 정보 조회
  Optional<TaskUser> findByTaskAndUser(Task task, User user);

  //특정 Task에 특정 유저가 참여 중인지 여부 확인
  boolean existsByTaskAndUser(Task task, User user);

  // 특정 Task 에 참여 중인 모든 유저 삭제
  void deleteAllByTask(Task task);
}

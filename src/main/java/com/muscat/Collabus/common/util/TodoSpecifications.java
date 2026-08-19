package com.muscat.Collabus.common.util;

import com.muscat.Collabus.Task.entity.TaskUser;
import com.muscat.Collabus.Todo.entity.Todo;
import com.muscat.Collabus.enums.status.TodoStatus;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

/**
 * 진행률 집계 조건
 */
public final class TodoSpecifications {

  private TodoSpecifications() {
  }

  public static Specification<Todo> inWorkspace(Long workspaceId) {
    return (root, query, cb) ->
        cb.equal(root.get("task").get("workspace").get("id"), workspaceId);
  }

  // TaskUser 에 해당 사용자 행이 있는 Task 의 Todo 만
  public static Specification<Todo> inTaskParticipatedBy(Long userId) {
    return (root, query, cb) -> {
      Subquery<Integer> sub = query.subquery(Integer.class);
      Root<TaskUser> taskUser = sub.from(TaskUser.class);
      sub.select(cb.literal(1))
          .where(
              cb.equal(taskUser.get("task"), root.get("task")),
              cb.equal(taskUser.get("user").get("id"), userId));
      return cb.exists(sub);
    };
  }

  public static Specification<Todo> inTask(Long taskId) {
    return (root, query, cb) -> cb.equal(root.get("task").get("id"), taskId);
  }

  public static Specification<Todo> hasStatus(TodoStatus status) {
    return (root, query, cb) -> cb.equal(root.get("status"), status);
  }
}

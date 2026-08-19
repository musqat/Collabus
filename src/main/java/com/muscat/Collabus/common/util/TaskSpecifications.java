package com.muscat.Collabus.common.util;

import com.muscat.Collabus.Task.entity.Task;
import com.muscat.Collabus.Task.entity.TaskUser;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

/**
 * Task 목록 조회 조건. 서비스에서 필요한 것만 골라 합친다.
 */
public final class TaskSpecifications {

  private static final char ESCAPE = '\\';

  private TaskSpecifications() {
  }

  public static Specification<Task> inWorkspace(Long workspaceId) {
    return (root, query, cb) -> cb.equal(root.get("workspace").get("id"), workspaceId);
  }

  /**
   * TaskUser 에 해당 사용자 행이 있는 Task 만. exists 서브쿼리로 확인한다.
   */
  public static Specification<Task> participatedBy(Long userId) {
    return (root, query, cb) -> {
      Subquery<Integer> sub = query.subquery(Integer.class);
      Root<TaskUser> taskUser = sub.from(TaskUser.class);
      sub.select(cb.literal(1))
          .where(
              cb.equal(taskUser.get("task"), root),
              cb.equal(taskUser.get("user").get("id"), userId));
      return cb.exists(sub);
    };
  }

  // 제목이나 설명에 부분 일치. 설명이 null 이면 빈 문자열로 본다
  public static Specification<Task> matches(String keyword) {
    return (root, query, cb) -> {
      String pattern = "%" + escape(keyword.toLowerCase()) + "%";
      return cb.or(
          cb.like(cb.lower(root.get("title")), pattern, ESCAPE),
          cb.like(cb.lower(cb.coalesce(root.get("description"), "")), pattern, ESCAPE));
    };
  }

  /**
   * LIKE 메타문자 %, _, 역슬래시 앞에 이스케이프 문자를 붙인다.
   */
  private static String escape(String keyword) {
    return keyword
        .replace(String.valueOf(ESCAPE), String.valueOf(ESCAPE) + ESCAPE)
        .replace("%", ESCAPE + "%")
        .replace("_", ESCAPE + "_");
  }
}

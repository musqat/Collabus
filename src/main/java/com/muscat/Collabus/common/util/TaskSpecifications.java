package com.muscat.Collabus.common.util;

import com.muscat.Collabus.Task.entity.Task;
import com.muscat.Collabus.Task.entity.TaskUser;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

/**
 * Task 목록 조회 조건
 * 조건을 따로 두고 서비스에서 필요한 것만 합친다
 */
public final class TaskSpecifications {

  private static final char ESCAPE = '\\';

  private TaskSpecifications() {
  }

  public static Specification<Task> inWorkspace(Long workspaceId) {
    return (root, query, cb) -> cb.equal(root.get("workspace").get("id"), workspaceId);
  }

  /**
   * 참여자인 Task만
   * 조인하면 참여자 수만큼 행이 늘어 페이지 건수가 어긋나므로 exists 로 확인한다.
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

  // 제목·설명 부분 일치. 설명은 비어 있을 수 있다
  public static Specification<Task> matches(String keyword) {
    return (root, query, cb) -> {
      String pattern = "%" + escape(keyword.toLowerCase()) + "%";
      return cb.or(
          cb.like(cb.lower(root.get("title")), pattern, ESCAPE),
          cb.like(cb.lower(cb.coalesce(root.get("description"), "")), pattern, ESCAPE));
    };
  }

  /**
   * LIKE를 값으로 취급한다.
   * 검색어에 % 나 _ 가 들어오면 전체가 걸린다 -> 안걸리게 필터링
   */
  private static String escape(String keyword) {
    return keyword
        .replace(String.valueOf(ESCAPE), String.valueOf(ESCAPE) + ESCAPE)
        .replace("%", ESCAPE + "%")
        .replace("_", ESCAPE + "_");
  }
}

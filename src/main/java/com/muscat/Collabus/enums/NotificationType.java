package com.muscat.Collabus.enums;

public enum NotificationType {
  TASK_ASSIGNED("태스크가 할당되었습니다"),
  TODO_COMPLETED("할일이 완료되었습니다"),
  TODO_REVIEW_REQUESTED("할일 검수가 요청되었습니다"),
  WORKSPACE_INVITED("워크스페이스에 초대되었습니다"),
  COMMENT_ADDED("새 댓글이 추가되었습니다"),
  TASK_DEADLINE_APPROACHING("태스크 마감일이 임박했습니다");

  private final String description;

  NotificationType(String description) {
    this.description = description;
  }

  public String getDescription() {
    return description;
  }
}

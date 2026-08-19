package com.muscat.Collabus.Task.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "워크스페이스 진행률")
public class WorkspaceProgressDto {

  @Schema(description = "전체 할일 수")
  private final long total;

  @Schema(description = "작업 중")
  private final long inProgress;

  @Schema(description = "검수 대기")
  private final long waitingReview;

  @Schema(description = "완료")
  private final long confirmed;
}

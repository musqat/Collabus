package com.muscat.Collabus.User.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

/**
 * 사용자 검색 결과
 */
@Getter
@Builder
@Schema(description = "사용자 요약")
public class UserSummaryDto {

    @Schema(description = "사용자 ID")
    private final Long id;

    @Schema(description = "닉네임")
    private final String nickname;

    @Schema(description = "nickname#tag 형식의 표시 이름")
    private final String displayName;
}

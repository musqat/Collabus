package com.muscat.Collabus.User.model;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "닉네임 수정 요청 DTO")
public class UpdateNicknameDto {
  @Schema(description = "변경할 닉네임", example = "가나다라 -> 라다나가")
  @NotBlank(message = "닉네임은 필수입니다.")
  @Size(min = 2, max = 20, message = "닉네임은 2자 이상 20자 이하여야 합니다.")
  private String nickname;
}

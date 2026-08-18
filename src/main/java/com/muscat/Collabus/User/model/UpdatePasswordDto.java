package com.muscat.Collabus.User.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "비밀번호 수정 요청 DTO")
public class UpdatePasswordDto {

  @Schema(description = "현재 비밀번호", example = "currentPassword123")
  @NotBlank(message = "현재 비밀번호를 입력해주세요.")
  private String currentPassword;

  @Schema(description = "새 비밀번호 (8자 이상, 영문+숫자 필수)", example = "newPassword123!")
  @NotBlank
  @Size(min = 8, message = "비밀번호는 최소 8자 이상이어야 합니다.")
  @Pattern(
      regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$",
      message = "비밀번호는 영문자와 숫자를 모두 포함해야 합니다."
  )
  private String password;
}

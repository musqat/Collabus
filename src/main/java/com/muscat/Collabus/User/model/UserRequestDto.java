package com.muscat.Collabus.User.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "UserRequest", description = "사용자 요청 DTO")
public class UserRequestDto {

  @Schema(description = "이메일", example = "user@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
  @Email
  @NotBlank
  private String email;

  @Schema(description = "닉네임", example = "musqat", requiredMode = Schema.RequiredMode.REQUIRED)
  @NotBlank
  private String nickname;

  @Schema(description = "비밀번호 (8자 이상, 영문+숫자 필수)", example = "secure123!", requiredMode = Schema.RequiredMode.REQUIRED)
  @NotBlank
  @Size(min = 8, message = "비밀번호는 최소 8자 이상이어야 합니다.")
  @Pattern(
      regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$",
      message = "비밀번호는 영문자와 숫자를 모두 포함해야 합니다."
  )
  private String password;
}

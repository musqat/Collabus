package com.muscat.Collabus.User.controller;

import static com.muscat.Collabus.enums.response.CommonResponse.*;
import static com.muscat.Collabus.enums.response.UserResponse.*;

import com.muscat.Collabus.User.model.*;
import com.muscat.Collabus.User.service.UserService;
import com.muscat.Collabus.common.dto.ResponseDto;
import com.muscat.Collabus.config.jwt.JwtUtil;
import com.muscat.Collabus.config.token.RefreshTokenService;
import com.muscat.Collabus.config.token.TokenResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/api/users", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "User API", description = "회원 관련 기능 제공 (회원가입, 조회, 수정, 삭제, 로그인)")
@Validated
public class UserController {

  private final UserService userService;
  private final JwtUtil jwtUtil;
  private final RefreshTokenService refreshTokenService;

  @Operation(
      summary = "회원가입",
      description = "새로운 유저를 등록합니다.",
      responses = {
          @ApiResponse(responseCode = "201", description = "회원가입 성공",
              content = @Content(schema = @Schema(implementation = ResponseDto.class))),
          @ApiResponse(responseCode = "409", description = "이메일 중복",
              content = @Content(schema = @Schema(implementation = ResponseDto.class)))
      }
  )
  @PostMapping("/register")
  public ResponseEntity<ResponseDto> register(@RequestBody @Valid UserRequestDto dto) {
    userService.registerUser(dto);
    return ResponseEntity.status(HttpStatus.CREATED).body(new ResponseDto(USER_CREATED));
  }

  @Operation(
      summary = "닉네임 검색",
      description = "닉네임에 포함된 키워드로 유저를 검색합니다.",
      responses = {
          @ApiResponse(responseCode = "200", description = "검색 성공",
              content = @Content(schema = @Schema(implementation = ResponseDto.class)))
      }
  )
  @GetMapping("/search")
  public ResponseEntity<ResponseDto> searchUsers(@RequestParam String keyword) {
    List<UserResponseDto> result = userService.searchByNickname(keyword);
    return ResponseEntity.ok(new ResponseDto(SUCCESS, result));
  }

  @Operation(
      summary = "유저 단건 조회",
      description = "displayName(nickname#tag)으로 유저 정보를 조회합니다.",
      responses = {
          @ApiResponse(responseCode = "200", description = "조회 성공",
              content = @Content(schema = @Schema(implementation = ResponseDto.class))),
          @ApiResponse(responseCode = "404", description = "사용자 없음",
              content = @Content(schema = @Schema(implementation = ResponseDto.class)))
      }
  )
  @GetMapping("/{displayName}")
  public ResponseEntity<ResponseDto> getUser(@PathVariable String displayName) {
    return userService.findByDisplayName(displayName)
        .map(user -> ResponseEntity.ok(new ResponseDto(SUCCESS, user)))
        .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(new ResponseDto(RESOURCE_NOT_FOUND)));
  }

  @Operation(
      summary = "닉네임 변경",
      description = "유저의 닉네임을 수정하고, displayName도 자동 갱신합니다.",
      responses = {
          @ApiResponse(responseCode = "200", description = "수정 성공",
              content = @Content), // 실제로 본문이 없으므로 content 비움
          @ApiResponse(responseCode = "400", description = "잘못된 요청",
              content = @Content(schema = @Schema(implementation = ResponseDto.class)))
      }
  )
  @PatchMapping("/nickname/{id}")
  @PreAuthorize("#id == principal.userId or hasRole('ADMIN')")
  public ResponseEntity<Void> updateNickname(
      @PathVariable Long id,
      @RequestBody UpdateNicknameDto dto) {
    userService.updateNickname(id, dto.getNickname());
    return ResponseEntity.ok().build();
  }

  @Operation(
      summary = "비밀번호 변경",
      description = "유저의 비밀번호를 변경합니다. 현재 비밀번호 확인이 필요하며, "
          + "변경 시 기존 Refresh Token 이 무효화되어 다른 기기 세션이 종료됩니다.",
      responses = {
          @ApiResponse(responseCode = "200", description = "변경 성공",
              content = @Content), // 본문 없음
          @ApiResponse(responseCode = "400", description = "잘못된 요청 / 현재 비밀번호 불일치",
              content = @Content(schema = @Schema(implementation = ResponseDto.class)))
      }
  )
  @PatchMapping("/password/{id}")
  @PreAuthorize("#id == principal.userId or hasRole('ADMIN')")
  public ResponseEntity<Void> updatePassword(
      @PathVariable Long id,
      @RequestBody @Valid UpdatePasswordDto dto) {
    userService.updatePassword(id, dto.getCurrentPassword(), dto.getPassword());
    return ResponseEntity.ok().build();
  }

  @Operation(
      summary = "회원 삭제",
      description = "이메일로 회원을 삭제합니다.",
      responses = {
          @ApiResponse(responseCode = "200", description = "삭제 성공",
              content = @Content(schema = @Schema(implementation = ResponseDto.class))),
          @ApiResponse(responseCode = "400", description = "삭제 실패",
              content = @Content(schema = @Schema(implementation = ResponseDto.class)))
      }
  )
  @DeleteMapping("/delete")
  @PreAuthorize("#email == principal.username or hasRole('ADMIN')")
  public ResponseEntity<ResponseDto> delete(@RequestParam String email) {
    boolean isDeleted = userService.deleteUser(email);
    return ResponseEntity.status(isDeleted ? HttpStatus.OK : HttpStatus.BAD_REQUEST)
        .body(new ResponseDto(isDeleted ? SUCCESS : DELETE_FAILED));
  }

  @Operation(
      summary = "로그인",
      description = "이메일과 비밀번호로 로그인하고 Access/Refresh 토큰을 발급합니다.",
      responses = {
          @ApiResponse(responseCode = "200", description = "로그인 성공",
              content = @Content(schema = @Schema(implementation = ResponseDto.class))),
          @ApiResponse(responseCode = "401", description = "로그인 실패 (인증 실패)",
              content = @Content(schema = @Schema(implementation = ResponseDto.class)))
      }
  )
  @PostMapping("/login")
  public ResponseEntity<ResponseDto> login(@RequestBody @Valid LoginDto request) {
    String email = request.getEmail();

    // 계정 잠금 확인 (5회 실패 시 10분 잠금)
    if (refreshTokenService.isAccountLocked(email)) {
      return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
          .body(new ResponseDto(LOGIN_ATTEMPTS_EXCEEDED));
    }

    try {
      var user = userService.login(email, request.getPassword());

      // 로그인 성공 — 실패 횟수 초기화
      refreshTokenService.resetLoginFailure(email);

      String accessToken = jwtUtil.generateToken(user.getId(), user.getEmail(), user.getRole().name(), user.getDisplayName());
      String refreshToken = jwtUtil.generateRefreshToken(user.getEmail());
      refreshTokenService.saveRefreshToken(user.getEmail(), refreshToken,
          jwtUtil.getRefreshExpiration());

      LoginResponseDto loginResponse = LoginResponseDto.builder()
          .id(user.getId())
          .email(user.getEmail())
          .nickname(user.getNickname())
          .displayName(user.getDisplayName())
          .role(user.getRole())
          .accessToken(accessToken)
          .refreshToken(refreshToken)
          .build();

      return ResponseEntity.ok(new ResponseDto(SUCCESS, loginResponse));
    } catch (com.muscat.Collabus.common.exception.ResourceNotFoundException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(new ResponseDto(EMAIL_NOT_FOUND));
    } catch (IllegalArgumentException e) {
      // 실패 횟수 증가
      int failures = refreshTokenService.incrementLoginFailure(email);
      int remaining = Math.max(0, 5 - failures);
      if (remaining == 0) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
            .body(new ResponseDto(LOGIN_ATTEMPTS_EXCEEDED));
      }
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(new ResponseDto(INVALID_PASSWORD));
    }
  }

  @Operation(
      summary = "로그아웃",
      description = "RefreshToken 삭제 및 AccessToken 블랙리스트 등록",
      responses = {
          @ApiResponse(responseCode = "200", description = "로그아웃 성공",
              content = @Content(schema = @Schema(implementation = ResponseDto.class))),
          @ApiResponse(responseCode = "400", description = "잘못된 요청 (Authorization 헤더 오류)",
              content = @Content(schema = @Schema(implementation = ResponseDto.class)))
      }
  )
  @PostMapping("/logout")
  public ResponseEntity<ResponseDto> logout(@RequestHeader("Authorization") String authHeader) {
    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      return ResponseEntity.badRequest().body(new ResponseDto(BAD_REQUEST));
    }

    String token = authHeader.substring(7);
    String email = jwtUtil.getEmailFromToken(token);

    refreshTokenService.deleteRefreshToken(email);
    long remaining = jwtUtil.getRemainingMillis(token);
    refreshTokenService.blacklistAccessToken(token, remaining);

    return ResponseEntity.ok(new ResponseDto(SUCCESS));
  }

  @Operation(
      summary = "관리자 생성",
      description = "관리자 계정을 생성합니다. (ADMIN 권한 필요)",
      responses = {
          @ApiResponse(responseCode = "201", description = "관리자 생성 성공",
              content = @Content(schema = @Schema(implementation = ResponseDto.class))),
          @ApiResponse(responseCode = "403", description = "권한 없음 (ADMIN 아님)",
              content = @Content(schema = @Schema(implementation = ResponseDto.class)))
      }
  )
  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping("/create-admin")
  public ResponseEntity<ResponseDto> createAdmin(@RequestBody @Valid UserRequestDto dto) {
    userService.createAdmin(dto);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(new ResponseDto(USER_CREATED));
  }
}

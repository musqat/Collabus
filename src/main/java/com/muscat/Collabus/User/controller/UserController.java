package com.muscat.Collabus.User.controller;

import com.muscat.Collabus.User.entity.User;
import com.muscat.Collabus.User.model.LoginDto;
import com.muscat.Collabus.User.model.UserDto;
import com.muscat.Collabus.User.service.UserService;
import com.muscat.Collabus.common.dto.ResponseDto;
import com.muscat.Collabus.config.jwt.JwtUtil;
import com.muscat.Collabus.enums.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "User API", description = "회원 관련 기능 제공 (회원가입, 조회, 수정, 삭제, 로그인)")
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/api/users", produces = MediaType.APPLICATION_JSON_VALUE)
@Validated
public class UserController {

  private final UserService userService;
  private final JwtUtil jwtUtil;

  @Operation(
      summary = "회원가입",
      description = "새로운 유저를 등록합니다.",
      responses = {
          @ApiResponse(responseCode = "201", description = "회원가입 성공"),
          @ApiResponse(responseCode = "409", description = "이메일 중복"),
          @ApiResponse(responseCode = "500", description = "서버의 문제가 발생하였습니다.")
      }
  )
  @PostMapping("/register")
  public ResponseEntity<ResponseDto> register(@RequestBody @Valid UserDto dto) {
    userService.registerUser(dto);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(new ResponseDto(UserResponse.USER_CREATED));
  }

  @Operation(
      summary = "닉네임 검색",
      description = "닉네임 부분으로 유저를 검색합니다.",
      responses = {
          @ApiResponse(responseCode = "200", description = "검색 성공"),
          @ApiResponse(responseCode = "400", description = "잘못된 요청 형식"),
          @ApiResponse(responseCode = "500", description = "서버의 문제가 발생하였습니다.")
      }
  )
  @GetMapping("/search")
  public ResponseEntity<?> searchUsers(
      @RequestParam String keyword) {
    List<UserDto> result = userService.searchByNickname(keyword);
    return ResponseEntity.ok(result);
  }

  @Operation(
      summary = "유저 단건 조회",
      description = "displayName으로 유저 정보를 조회합니다.",
      responses = {
          @ApiResponse(responseCode = "200", description = "조회 성공"),
          @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없습니다."),
          @ApiResponse(responseCode = "500", description = "서버의 문제가 발생하였습니다.")
      }
  )
  @GetMapping("/{displayName}")
  public ResponseEntity<?> getUser(
      @PathVariable String displayName) {
    return userService.findByDisplayName(displayName)
        .<ResponseEntity<?>>map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(new ResponseDto(UserResponse.USER_NOT_FOUND)));
  }

  @Operation(
      summary = "회원 정보 수정",
      description = "회원 정보를 수정합니다.",
      responses = {
          @ApiResponse(responseCode = "200", description = "수정 성공"),
          @ApiResponse(responseCode = "400", description = "수정 실패"),
          @ApiResponse(responseCode = "500", description = "서버의 문제가 발생하였습니다.")
      }
  )
  @PutMapping("/update")
  public ResponseEntity<ResponseDto> update(@RequestBody @Valid UserDto dto) {
    boolean isUpdated = userService.updateUser(dto);
    return isUpdated
        ? ResponseEntity.ok(new ResponseDto(UserResponse.SUCCESS))
        : ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(new ResponseDto(UserResponse.UPDATE_FAILED));
  }

  @Operation(
      summary = "회원 삭제",
      description = "회원을 삭제합니다.",
      responses = {
          @ApiResponse(responseCode = "200", description = "삭제 성공"),
          @ApiResponse(responseCode = "400", description = "삭제 실패"),
          @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없습니다."),
          @ApiResponse(responseCode = "500", description = "서버의 문제가 발생하였습니다.")
      }
  )
  @DeleteMapping("/delete")
  public ResponseEntity<ResponseDto> delete(
      @RequestParam String email) {
    boolean isDeleted = userService.deleteUser(email);
    return isDeleted
        ? ResponseEntity.ok(new ResponseDto(UserResponse.SUCCESS))
        : ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(new ResponseDto(UserResponse.DELETE_FAILED));
  }

  @Operation(
      summary = "로그인",
      description = "이메일과 비밀번호로 로그인하고 JWT를 발급합니다.",
      responses = {
          @ApiResponse(responseCode = "200", description = "로그인 성공"),
          @ApiResponse(responseCode = "401", description = "로그인 실패"),
          @ApiResponse(responseCode = "500", description = "서버의 문제가 발생하였습니다.")
      }
  )

  @PostMapping("/login")
  public ResponseEntity<ResponseDto> login(@RequestBody @Valid LoginDto request) {
    try {
      User user = userService.login(request.getEmail(), request.getPassword());
      String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());

      return ResponseEntity.ok(new ResponseDto(UserResponse.LOGIN_OK, token));
    } catch (IllegalArgumentException e) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(new ResponseDto(UserResponse.LOGIN_FAILED));
    }
  }

}

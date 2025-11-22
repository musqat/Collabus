package com.muscat.Collabus.User.service;

import com.muscat.Collabus.User.model.UserRequestDto;
import com.muscat.Collabus.User.model.UserResponseDto;
import java.util.List;
import java.util.Optional;

public interface UserService {

  // 회원 가입
  void registerUser(UserRequestDto dto);

  // 닉네임 키워드로 사용자 검색
  List<UserResponseDto> searchByNickname(String keyword);

  // displayName (nickname#tag)으로 단건 조회
  Optional<UserResponseDto> findByDisplayName(String displayName);

  // 회원 정보 수정 (닉네임)
  void updateNickname(Long userId, String newNickname);

  // 회원 정보 수정 (비밀번호)
  void updatePassword(Long userId, String newPassword);

  // 회원 삭제 (email로 식별)
  boolean deleteUser(String email);

  // 로그인
  UserResponseDto login(String email, String password);

  // ADMIN 계정 생성
  void createAdmin(UserRequestDto dto);
}

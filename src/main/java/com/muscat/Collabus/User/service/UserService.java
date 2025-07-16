package com.muscat.Collabus.User.service;

import com.muscat.Collabus.User.entity.User;
import com.muscat.Collabus.User.model.UserDto;
import java.util.List;
import java.util.Optional;

public interface UserService {

  /**
   * 회원 가입
   */
  UserDto registerUser(UserDto dto);

  /**
   * 회원 닉네임 키워드로 유사 이름 검색
   */
  List<UserDto> searchByNickname(String keyword);

  /**
   * 회원 닉네임으로 단건 조회
   */
  Optional<UserDto> findByDisplayName(String displayName); // nickname#tag

  /**
   * 회원 수정
   */
  boolean updateUser(UserDto dto);

  /**
   * 회원 삭제
   */
  boolean deleteUser(String email);

  /**
   * 회원 로그인
   */
  User login(String email, String password);
}

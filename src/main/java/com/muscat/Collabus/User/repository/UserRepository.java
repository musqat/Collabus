package com.muscat.Collabus.User.repository;

import com.muscat.Collabus.User.entity.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

  // email 조회
  Optional<User> findByEmail(String email);

  // displayName 조회
  Optional<User> findByDisplayName(String displayName);

  // displayName 확인
  boolean existsByDisplayName(String displayName);

  // 유저 검색
  List<User> findByNicknameContainingIgnoreCase(String keyword);
}

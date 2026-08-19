package com.muscat.Collabus.User.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import com.muscat.Collabus.User.entity.User;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    // email 조회
    Optional<User> findByEmail(String email);

    // 검색은 페이징이라 조건을 쿼리에 넣는다. 자른 뒤에 거르면 페이지마다 건수가 어긋난다
    Page<User> findByNicknameContainingIgnoreCaseAndDeletedAtIsNull(String keyword,
                                                                    Pageable pageable);

    // displayName 조회
    Optional<User> findByDisplayName(String displayName);

    // displayName 확인
    boolean existsByDisplayName(String displayName);

    // 유저 다중 검색
    Page<User> findByNicknameContainingIgnoreCase(String keyword, Pageable pageable);

    // displayName 중복 체크
    boolean existsByDisplayNameAndIdNot(String newDisplayName, Long userId);
}

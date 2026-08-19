package com.muscat.Collabus.User.service;

import org.springframework.data.domain.Pageable;
import com.muscat.Collabus.common.dto.PageResponseDto;
import com.muscat.Collabus.User.model.UserSummaryDto;
import com.muscat.Collabus.User.model.UserRequestDto;
import com.muscat.Collabus.User.model.UserResponseDto;
import java.util.Optional;

public interface UserService {

    // 회원을 등록한다. displayName 은 nickname#tag 형식이고 tag 는 자동 생성한다
    void registerUser(UserRequestDto dto);

    // 닉네임 부분 일치로 사용자를 찾는다
    PageResponseDto<UserSummaryDto> searchByNickname(String keyword, Pageable pageable);

    // displayName (nickname#tag) 으로 단건 조회
    Optional<UserSummaryDto> findByDisplayName(String displayName);

    // 닉네임을 바꾼다. tag 는 그대로 둔다
    void updateNickname(Long userId, String newNickname);

    // 비밀번호를 바꾼다. Refresh Token 을 지워 다른 기기 세션을 끊는다
    void updatePassword(Long userId, String currentPassword, String newPassword);

    // email 로 찾아 회원을 삭제한다
    boolean deleteUser(String email);

    // 로그인한다
    UserResponseDto login(String email, String password);

    // ADMIN 계정을 만든다
    void createAdmin(UserRequestDto dto);
}

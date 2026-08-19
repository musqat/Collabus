package com.muscat.Collabus.User.entity;

import java.time.LocalDateTime;
import com.muscat.Collabus.common.entity.BaseEntity;
import com.muscat.Collabus.enums.role.SystemRole;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.security.crypto.password.PasswordEncoder;


@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Builder
@Table(name = "users")
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // PK

    @Column(nullable = false, unique = true)
    private String email; // 이메일

    @Column(nullable = false)
    private String nickname; // 닉네임

    @Column(nullable = false)
    private String password; // 패스워드

    @Column(nullable = false)
    private String tag; // 4자리 숫자(자동생성)

    @Column(nullable = false, unique = true)
    private String displayName; // nickname#tag 조합

    @Enumerated(EnumType.STRING)
    private SystemRole role; // 시스템 역할 (일반 유저, 운영자)


    // 탈퇴 시각. null 이면 활성 계정이다
    private LocalDateTime deletedAt;

    public boolean isDeleted() {
        return deletedAt != null;
    }

    // 탈퇴 처리
    public void withdraw(String maskedEmail, String maskedDisplayName, String unusablePassword) {
        this.deletedAt = LocalDateTime.now();
        this.email = maskedEmail;
        this.nickname = "탈퇴한 사용자";
        this.displayName = maskedDisplayName;
        this.password = unusablePassword;
    }

    public void changeNickname(String newNickname) {
        this.nickname = newNickname;
        this.displayName = newNickname + "#" + this.tag;
    }

    public void changePassword(String newPassword, PasswordEncoder encoder) {
        this.password = encoder.encode(newPassword);
    }


    public void assignRole(SystemRole role) {
        this.role = role;
    }
}

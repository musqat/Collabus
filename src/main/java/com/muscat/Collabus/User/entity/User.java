package com.muscat.Collabus.User.entity;

import com.muscat.Collabus.common.entity.BaseEntity;
import com.muscat.Collabus.enums.SystemRole;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;


@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Entity
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

}

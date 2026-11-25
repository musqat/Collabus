package com.muscat.Collabus.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * 생성 · 수정 시각과 작성자를 엔티티에 채운다. 컨트롤러 슬라이스 테스트가 JPA 없이 뜨도록 메인 클래스에서 분리
 */
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {

}

package com.muscat.Collabus.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.muscat.Collabus.User.entity.User;
import com.muscat.Collabus.User.repository.UserRepository;
import com.muscat.Collabus.Workspace.entity.Workspace;
import com.muscat.Collabus.Workspace.repository.WorkspaceRepository;
import com.muscat.Collabus.config.security.CustomUserDetails;
import com.muscat.Collabus.enums.role.SystemRole;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

/**
 * BaseEntity 의 감사 필드가 실제로 채워지는지 확인한다.
 * 감사 값은 Spring Data 가 리플렉션으로 주입하므로 setter 유무와 무관해야 한다.
 */
@SpringBootTest
@Transactional
@DisplayName("JPA Auditing 통합 테스트")
class AuditingIntegrationTest {

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private WorkspaceRepository workspaceRepository;

  @Autowired
  private EntityManager entityManager;

  private User founder;

  @BeforeEach
  void setUp() {
    founder = userRepository.save(User.builder()
        .email("auditor@test.com")
        .nickname("auditor")
        .password("encoded")
        .tag("0001")
        .displayName("auditor#0001")
        .role(SystemRole.USER)
        .build());
  }

  @AfterEach
  void clearContext() {
    SecurityContextHolder.clearContext();
  }

  private Workspace saveWorkspace() {
    Workspace saved = workspaceRepository.save(Workspace.builder()
        .workspaceName("audit ws")
        .description("desc")
        .founder(founder)
        .build());
    entityManager.flush();
    return saved;
  }

  @Test
  @DisplayName("저장하면 생성 시각과 수정 시각이 채워진다")
  void createdAtAndUpdatedAtArePopulated() {
    Workspace saved = saveWorkspace();

    assertThat(saved.getCreatedAt()).isNotNull();
    assertThat(saved.getUpdatedAt()).isNotNull();
  }

  @Test
  @DisplayName("비로그인 상태에서 저장하면 감사자는 system 이다")
  void auditorFallsBackToSystem() {
    Workspace saved = saveWorkspace();

    assertThat(saved.getCreatedBy()).isEqualTo("system");
  }

  @Test
  @DisplayName("로그인 상태에서 저장하면 감사자에 사용자 이메일이 남는다")
  void auditorIsCurrentUserEmail() {
    CustomUserDetails principal = new CustomUserDetails(founder);
    SecurityContextHolder.getContext().setAuthentication(
        new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));

    Workspace saved = saveWorkspace();

    assertThat(saved.getCreatedBy()).isEqualTo("auditor@test.com");
  }

  @Test
  @DisplayName("수정하면 수정 시각이 갱신된다")
  void updatedAtChangesOnModification() throws InterruptedException {
    Workspace saved = saveWorkspace();
    var firstUpdatedAt = saved.getUpdatedAt();

    Thread.sleep(10);
    saved.update("renamed ws", "renamed desc");
    entityManager.flush();

    assertThat(saved.getUpdatedAt()).isAfter(firstUpdatedAt);
    // 생성 시각은 그대로여야 한다
    assertThat(saved.getCreatedAt()).isBefore(saved.getUpdatedAt());
  }
}

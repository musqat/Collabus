package com.muscat.Collabus.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
import javax.sql.DataSource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * 마이그레이션을 실제 MySQL 에 적용해 검증한다.

 * 다른 테스트는 H2 + ddl-auto 로 돌기 때문에 마이그레이션이 실행되지 않는다.
 * 마이그레이션이 깨져도 알 수 없으므로 여기서만 DB 를 띄운다.
 * Hibernate 는 validate 로 두어, 엔티티와 마이그레이션 결과가 어긋나면 기동이 실패한다.
 */
@SpringBootTest
@Testcontainers
@EnabledIf(value = "dockerAvailable", disabledReason = "Docker 를 사용할 수 없는 환경에서는 건너뛴다")
@TestPropertySource(properties = {
    "spring.flyway.enabled=true",
    "spring.jpa.hibernate.ddl-auto=validate",
    "spring.jpa.database-platform=org.hibernate.dialect.MySQLDialect"
})
@DisplayName("Flyway 마이그레이션 통합 테스트")
class FlywayMigrationIntegrationTest {

  /**
   * 로컬 개발 환경에는 Docker 가 없을 수 있어 조건부로 실행한다. CI 에서는 항상 돈다.
   */
  static boolean dockerAvailable() {
    try {
      return DockerClientFactory.instance().isDockerAvailable();
    } catch (Throwable e) {
      return false;
    }
  }

  @Container
  @ServiceConnection
  static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0");

  @Autowired
  private DataSource dataSource;

  private List<String> query(String sql, String column) throws Exception {
    List<String> values = new ArrayList<>();
    try (var connection = dataSource.getConnection();
        Statement statement = connection.createStatement();
        ResultSet rs = statement.executeQuery(sql)) {
      while (rs.next()) {
        values.add(rs.getString(column));
      }
    }
    return values;
  }

  /**
   * 마이그레이션 파일에서 버전을 뽑는다
   */
  private List<String> expectedVersions() throws Exception {
    URL location = getClass().getClassLoader().getResource("db/migration");
    assertThat(location).as("db/migration 을 찾을 수 없다").isNotNull();

    try (Stream<Path> files = Files.list(Path.of(location.toURI()))) {
      return files
          .map(path -> path.getFileName().toString())
          .filter(name -> name.startsWith("V") && name.endsWith(".sql"))
          .map(name -> name.substring(1, name.indexOf("__")))
          .sorted(Comparator.comparingInt(Integer::parseInt))
          .toList();
    }
  }

  @Test
  @DisplayName("마이그레이션이 순서대로 모두 적용된다")
  void allMigrationsApplied() throws Exception {
    List<String> applied = query(
        "SELECT version FROM flyway_schema_history WHERE success = 1 ORDER BY installed_rank",
        "version");

    assertThat(applied).containsExactlyElementsOf(expectedVersions());
  }

  @Test
  @DisplayName("엔티티와 스키마가 일치한다")
  void schemaMatchesEntities() {
    assertThat(dataSource).isNotNull();
  }

  @Test
  @DisplayName("V3 이후 Todo 마감일 컬럼이 DATE 타입이다")
  void todoDueDateIsDateType() throws Exception {
    List<String> types = query("""
        SELECT DATA_TYPE FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'todos' AND COLUMN_NAME = 'due_date'
        """, "DATA_TYPE");

    assertThat(types).containsExactly("date");
  }

  @Test
  @DisplayName("작업 내용과 댓글 본문은 TEXT 라 긴 한글도 담긴다")
  void contentColumnsAreText() throws Exception {
    // @Lob 이 tinytext(255바이트)로 매핑돼 한글 85자에서 저장이 실패하던 문제의 회귀 방지
    List<String> types = query("""
        SELECT DATA_TYPE FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME IN ('todo_work', 'todo_comments')
          AND COLUMN_NAME = 'content'
        """, "DATA_TYPE");

    assertThat(types).hasSize(2).containsOnly("text");
  }

  @Test
  @DisplayName("시드 데이터가 들어간다")
  void seedDataApplied() throws Exception {
    List<String> counts = query("SELECT COUNT(*) AS c FROM users", "c");

    assertThat(Integer.parseInt(counts.get(0))).isPositive();
  }
}

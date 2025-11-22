package com.muscat.Collabus.common.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("DisplayNameUtil 단위 테스트")
class DisplayNameUtilTest {

  @Test
  @DisplayName("고유한 태그 생성 - 중복 없음")
  void generateUniqueTag_Success_NoDuplicate() {
    // Given
    String nickname = "testuser";
    Set<String> existingDisplayNames = new HashSet<>();

    // When
    String tag = DisplayNameUtil.generateUniqueTag(nickname, existingDisplayNames::contains);

    // Then
    assertThat(tag).isNotNull();
    assertThat(tag).hasSize(4);
    assertThat(tag).matches("\\d{4}"); // 4자리 숫자
  }

  @Test
  @DisplayName("고유한 태그 생성 - 중복 회피")
  void generateUniqueTag_Success_AvoidDuplicate() {
    // Given
    String nickname = "testuser";
    Set<String> existingDisplayNames = new HashSet<>();
    existingDisplayNames.add("testuser#0001");
    existingDisplayNames.add("testuser#0002");
    existingDisplayNames.add("testuser#0003");

    // When
    String tag = DisplayNameUtil.generateUniqueTag(nickname, existingDisplayNames::contains);
    String displayName = nickname + "#" + tag;

    // Then
    assertThat(tag).isNotNull();
    assertThat(tag).hasSize(4);
    assertThat(displayName).isNotIn(existingDisplayNames);
  }

  @Test
  @DisplayName("고유한 태그 생성 - 여러 개 생성 시 중복 없음")
  void generateUniqueTag_Success_MultipleGeneration() {
    // Given
    String nickname = "testuser";
    Set<String> existingDisplayNames = new HashSet<>();

    // When - 100개의 태그 생성
    for (int i = 0; i < 100; i++) {
      String tag = DisplayNameUtil.generateUniqueTag(nickname, existingDisplayNames::contains);
      String displayName = nickname + "#" + tag;
      existingDisplayNames.add(displayName);
    }

    // Then - 모두 고유해야 함
    assertThat(existingDisplayNames).hasSize(100);
  }

  @Test
  @DisplayName("태그 형식 검증 - 4자리 숫자")
  void generateUniqueTag_Format_FourDigits() {
    // Given
    String nickname = "testuser";

    // When
    String tag = DisplayNameUtil.generateUniqueTag(nickname, s -> false);

    // Then
    assertThat(tag).hasSize(4);
    assertThat(tag).matches("\\d{4}");
    int tagNumber = Integer.parseInt(tag);
    assertThat(tagNumber).isBetween(0, 9999);
  }
}

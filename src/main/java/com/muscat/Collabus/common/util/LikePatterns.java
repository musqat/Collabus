package com.muscat.Collabus.common.util;

/**
 * 검색어를 LIKE 부분 일치 패턴으로 바꾼다.
 */
public final class LikePatterns {

  public static final char ESCAPE = '\\';

  private LikePatterns() {
  }

  // 소문자로 맞추고 메타문자를 이스케이프한 %검색어% 패턴
  public static String contains(String keyword) {
    String escaped = keyword.toLowerCase()
        .replace(String.valueOf(ESCAPE), String.valueOf(ESCAPE) + ESCAPE)
        .replace("%", ESCAPE + "%")
        .replace("_", ESCAPE + "_");
    return "%" + escaped + "%";
  }
}

package com.muscat.Collabus.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.function.Function;
import lombok.Getter;
import org.springframework.data.domain.Page;

/**
 * 목록 응답 공통 포맷
 */
@Getter
@Schema(description = "페이지 응답")
public class PageResponseDto<T> {

  private final List<T> content;
  private final int page;
  private final int size;
  private final long totalElements;
  private final int totalPages;
  private final boolean hasNext;

  private PageResponseDto(Page<T> source) {
    this.content = source.getContent();
    this.page = source.getNumber();
    this.size = source.getSize();
    this.totalElements = source.getTotalElements();
    this.totalPages = source.getTotalPages();
    this.hasNext = source.hasNext();
  }

  public static <E, T> PageResponseDto<T> of(Page<E> source, Function<E, T> mapper) {
    return new PageResponseDto<>(source.map(mapper));
  }
}

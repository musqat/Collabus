package com.muscat.Collabus.common.util;

import jakarta.persistence.EntityManager;
import jakarta.persistence.metamodel.Attribute;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

/**
 * 엔티티 메타모델에 있는 속성과 따로 허용한 연관 경로만 정렬 조건으로 남긴다.
 */
@Component
@RequiredArgsConstructor
public class SortGuard {

  private final EntityManager entityManager;

  private final Map<Class<?>, Set<String>> cache = new ConcurrentHashMap<>();

  public Pageable apply(Pageable pageable, Class<?> entityType) {
    return apply(pageable, entityType, Set.of());
  }

  /**
   * extraPaths 에 적은 연관 경로도 함께 허용한다. (예: "taskManager.displayName")
   */
  public Pageable apply(Pageable pageable, Class<?> entityType, Set<String> extraPaths) {
    if (pageable.getSort().isUnsorted()) {
      return pageable;
    }

    Set<String> attributes = attributesOf(entityType);
    Sort filtered = Sort.by(pageable.getSort().stream()
        .filter(order -> attributes.contains(order.getProperty())
            || extraPaths.contains(order.getProperty()))
        .toList());

    if (filtered.equals(pageable.getSort())) {
      return pageable;
    }

    // 버린 조건을 뺀 나머지로 다시 만든다
    return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), filtered);
  }

  private Set<String> attributesOf(Class<?> entityType) {
    return cache.computeIfAbsent(entityType, type ->
        entityManager.getMetamodel().entity(type).getAttributes().stream()
            .map(Attribute::getName)
            .collect(Collectors.toUnmodifiableSet()));
  }
}

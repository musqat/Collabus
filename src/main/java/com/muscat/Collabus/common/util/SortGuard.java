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
 * 엔티티에 매핑된 속성으로만 정렬하게 한다.
 * ?sort=taskManager.password 처럼 연관을 타고 들어가는 정렬을 막는다.
 */
@Component
@RequiredArgsConstructor
public class SortGuard {

  private final EntityManager entityManager;

  private final Map<Class<?>, Set<String>> cache = new ConcurrentHashMap<>();

  public Pageable apply(Pageable pageable, Class<?> entityType) {
    if (pageable.getSort().isUnsorted()) {
      return pageable;
    }

    Set<String> attributes = attributesOf(entityType);
    Sort filtered = Sort.by(pageable.getSort().stream()
        .filter(order -> attributes.contains(order.getProperty()))
        .toList());

    if (filtered.equals(pageable.getSort())) {
      return pageable;
    }

    // 허용되지 않은 정렬은 조용히 버린다
    return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), filtered);
  }

  private Set<String> attributesOf(Class<?> entityType) {
    return cache.computeIfAbsent(entityType, type ->
        entityManager.getMetamodel().entity(type).getAttributes().stream()
            .map(Attribute::getName)
            .collect(Collectors.toUnmodifiableSet()));
  }
}

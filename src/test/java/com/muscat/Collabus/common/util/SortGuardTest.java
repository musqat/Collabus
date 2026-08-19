package com.muscat.Collabus.common.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.muscat.Collabus.Task.entity.Task;
import jakarta.persistence.EntityManager;
import jakarta.persistence.metamodel.Attribute;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.Metamodel;
import java.util.LinkedHashSet;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("정렬 속성 검증")
class SortGuardTest {

  @Mock
  private EntityManager entityManager;

  @Mock
  private Metamodel metamodel;

  @Mock
  private EntityType<Task> entityType;

  private SortGuard sortGuard;

  @BeforeEach
  void setUp() {
    // Task 에 매핑된 속성이 id, title, dueDate 뿐인 상황
    Set<Attribute<? super Task, ?>> attributes = new LinkedHashSet<>();
    attributes.add(attribute("id"));
    attributes.add(attribute("title"));
    attributes.add(attribute("dueDate"));

    when(entityManager.getMetamodel()).thenReturn(metamodel);
    when(metamodel.entity(Task.class)).thenReturn(entityType);
    when(entityType.getAttributes()).thenReturn(attributes);

    sortGuard = new SortGuard(entityManager);
  }

  @SuppressWarnings("unchecked")
  private Attribute<? super Task, ?> attribute(String name) {
    Attribute<? super Task, ?> attribute = org.mockito.Mockito.mock(Attribute.class);
    when(attribute.getName()).thenReturn(name);
    return attribute;
  }

  @Test
  @DisplayName("매핑된 속성은 그대로 둔다")
  void keepsMappedAttribute() {
    Pageable pageable = PageRequest.of(0, 20, Sort.by("dueDate").descending());

    assertThat(sortGuard.apply(pageable, Task.class).getSort()).isEqualTo(pageable.getSort());
  }

  @Test
  @DisplayName("연관을 타고 들어가는 정렬은 버린다")
  void dropsNestedPath() {
    Pageable pageable = PageRequest.of(0, 20, Sort.by("taskManager.password"));

    Pageable result = sortGuard.apply(pageable, Task.class);

    assertThat(result.getSort().isSorted()).isFalse();
    assertThat(result.getPageSize()).isEqualTo(20);
  }

  @Test
  @DisplayName("매핑되지 않은 속성만 골라 버린다")
  void dropsOnlyUnmapped() {
    Pageable pageable = PageRequest.of(1, 20, Sort.by("dueDate", "nosuchfield"));

    Pageable result = sortGuard.apply(pageable, Task.class);

    assertThat(result.getSort()).isEqualTo(Sort.by("dueDate"));
    assertThat(result.getPageNumber()).isEqualTo(1);
  }

  @Test
  @DisplayName("정렬이 없으면 메타모델을 보지 않는다")
  void skipsUnsorted() {
    Pageable pageable = PageRequest.of(0, 20);

    assertThat(sortGuard.apply(pageable, Task.class)).isSameAs(pageable);
    verify(entityManager, times(0)).getMetamodel();
  }

  @Test
  @DisplayName("같은 엔티티는 메타모델을 한 번만 조회한다")
  void cachesPerEntity() {
    Pageable pageable = PageRequest.of(0, 20, Sort.by("dueDate"));

    sortGuard.apply(pageable, Task.class);
    sortGuard.apply(pageable, Task.class);

    verify(entityManager, times(1)).getMetamodel();
  }
}

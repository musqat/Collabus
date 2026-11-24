package com.muscat.Collabus.common.exception;

import static org.assertj.core.api.Assertions.assertThat;

import com.muscat.Collabus.Todo.entity.Todo;
import com.muscat.Collabus.common.dto.ErrorResponseDto;
import com.muscat.Collabus.enums.response.CommonResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

@DisplayName("같은 순간에 겹친 저장 응답")
class GlobalExceptionHandlerLockTest {

  @Test
  @DisplayName("JPA 버전 충돌은 500 이 아니라 409 와 수정 충돌 문구로 내려간다")
  void optimisticLockIsConflict() {
    MockHttpServletRequest request = new MockHttpServletRequest("PATCH", "/api/todo/1");

    ResponseEntity<ErrorResponseDto> response = new GlobalExceptionHandler().handleOptimisticLock(
        new ObjectOptimisticLockingFailureException(Todo.class, 1L), request);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    assertThat(response.getBody().getMessage()).isEqualTo(CommonResponse.EDIT_CONFLICT.getMessage());
  }
}

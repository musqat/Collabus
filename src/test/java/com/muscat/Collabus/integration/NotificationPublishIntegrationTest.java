package com.muscat.Collabus.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.muscat.Collabus.Notification.dto.NotificationResponse;
import com.muscat.Collabus.Notification.repository.NotificationRepository;
import com.muscat.Collabus.Notification.service.NotificationService;
import com.muscat.Collabus.User.entity.User;
import com.muscat.Collabus.User.repository.UserRepository;
import com.muscat.Collabus.config.websocket.WebSocketService;
import com.muscat.Collabus.enums.NotificationType;
import com.muscat.Collabus.enums.role.SystemRole;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * 알림 저장부터 커밋 이후 전송까지를 실제 트랜잭션으로 확인한다.
 * 롤백하는 테스트에서는 커밋 이후 리스너가 불리지 않으므로 클래스에 @Transactional 을 두지 않고 직접 정리한다.
 */
@SpringBootTest
@DisplayName("알림 발행 경로 통합 테스트")
class NotificationPublishIntegrationTest {

  @Autowired
  private NotificationService notificationService;

  @Autowired
  private NotificationRepository notificationRepository;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private TransactionTemplate transactionTemplate;

  @MockBean
  private WebSocketService webSocketService;

  private Long userId;

  @BeforeEach
  void setUp() {
    userId = userRepository.save(User.builder()
        .email("receiver@test.com")
        .nickname("receiver")
        .password("로그인을 하지 않으므로 쓰이지 않는다")
        .tag("0001")
        .displayName("receiver#0001")
        .role(SystemRole.USER)
        .build()).getId();
  }

  @AfterEach
  void tearDown() {
    transactionTemplate.executeWithoutResult(status -> {
      notificationRepository.deleteByUser_Id(userId);
      userRepository.deleteById(userId);
    });
  }

  private void createNotification() {
    notificationService.createNotification(
        userId, NotificationType.TASK_ASSIGNED, "태스크가 할당되었습니다", 1L);
  }

  private long savedCount() {
    return notificationRepository.findByUser_IdOrderByIdDesc(userId, Pageable.unpaged()).size();
  }

  @Test
  @DisplayName("커밋되면 저장한 알림을 그 사용자에게 한 번 보낸다")
  void sendsAfterCommit() {
    transactionTemplate.executeWithoutResult(status -> {
      createNotification();

      // 커밋 전에는 전송 없음
      verify(webSocketService, never()).sendNotificationToUser(anyLong(), any());
    });

    verify(webSocketService, times(1))
        .sendNotificationToUser(eq(userId), any(NotificationResponse.class));
    assertThat(savedCount()).isEqualTo(1);
  }

  @Test
  @DisplayName("롤백되면 알림도 남지 않고 전송도 없다")
  void noSendOnRollback() {
    transactionTemplate.executeWithoutResult(status -> {
      createNotification();
      status.setRollbackOnly();
    });

    verify(webSocketService, never()).sendNotificationToUser(anyLong(), any());
    assertThat(savedCount()).isZero();
  }

  @Test
  @DisplayName("전송이 실패해도 커밋된 알림은 남는다")
  void keepsNotificationWhenSendFails() {
    doThrow(new IllegalStateException("브로커 연결 끊김"))
        .when(webSocketService).sendNotificationToUser(anyLong(), any());

    assertThatCode(() -> transactionTemplate.executeWithoutResult(status -> createNotification()))
        .doesNotThrowAnyException();

    assertThat(savedCount()).isEqualTo(1);
  }
}

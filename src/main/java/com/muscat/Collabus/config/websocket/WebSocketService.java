package com.muscat.Collabus.config.websocket;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WebSocketService {

  private final SimpMessagingTemplate messagingTemplate;

  // 특정 사용자에게 알림 메시지를 전송합니다.
  public void sendNotificationToUser(Long userId, Object message) {
    messagingTemplate.convertAndSendToUser(
        userId.toString(),
        "/queue/notifications",
        message
    );
  }
}

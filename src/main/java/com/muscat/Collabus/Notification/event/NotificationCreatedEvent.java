package com.muscat.Collabus.Notification.event;

import com.muscat.Collabus.Notification.dto.NotificationResponse;

/**
 * 알림이 저장된 뒤 발행되는 이벤트. 트랜잭션이 커밋된 후에만 WebSocket 으로 전송하기 위해 사용한다.
 */
public record NotificationCreatedEvent(Long userId, NotificationResponse notification) {

}

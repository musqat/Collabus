package com.muscat.Collabus.Notification.dto;

import com.muscat.Collabus.Notification.entity.Notification;
import com.muscat.Collabus.enums.NotificationType;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {

  private Long id;
  private NotificationType type;
  private String message;
  private Long relatedEntityId;
  private Boolean isRead;
  private LocalDateTime createdAt;

  public static NotificationResponse from(Notification notification) {
    return NotificationResponse.builder()
        .id(notification.getId())
        .type(notification.getType())
        .message(notification.getMessage())
        .relatedEntityId(notification.getRelatedEntityId())
        .isRead(notification.getIsRead())
        .createdAt(notification.getCreatedAt())
        .build();
  }
}

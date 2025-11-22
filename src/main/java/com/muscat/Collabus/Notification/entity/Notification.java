package com.muscat.Collabus.Notification.entity;

import com.muscat.Collabus.User.entity.User;
import com.muscat.Collabus.common.entity.BaseEntity;
import com.muscat.Collabus.enums.NotificationType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
@Table(name = "notifications")
public class Notification extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private NotificationType type;

  @Column(nullable = false)
  private String message;

  @Column
  private Long relatedEntityId; // taskId, todoId, workspaceId 등

  @Column(nullable = false)
  @Builder.Default
  private Boolean isRead = false;

  public void markAsRead() {
    this.isRead = true;
  }
}

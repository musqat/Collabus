package com.muscat.Collabus.Notification.repository;

import com.muscat.Collabus.Notification.entity.Notification;
import com.muscat.Collabus.User.entity.User;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

  // 사용자의 모든 알림 조회 (최신순)
  Page<Notification> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

  // 사용자의 읽지 않은 알림 조회
  List<Notification> findByUserAndIsReadFalseOrderByCreatedAtDesc(User user);

  Page<Notification> findByUserAndIsReadFalseOrderByCreatedAtDesc(User user, Pageable pageable);

  // 사용자의 읽지 않은 알림 개수
  Long countByUserAndIsReadFalse(User user);

  // 사용자의 최근 N개 알림 조회
  @Query("SELECT n FROM Notification n WHERE n.user = :user ORDER BY n.createdAt DESC")
  List<Notification> findRecentNotifications(@Param("user") User user,
      org.springframework.data.domain.Pageable pageable);
}

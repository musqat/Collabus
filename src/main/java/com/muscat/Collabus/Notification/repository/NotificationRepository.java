package com.muscat.Collabus.Notification.repository;

import com.muscat.Collabus.Notification.entity.Notification;
import com.muscat.Collabus.User.entity.User;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

  // 사용자의 알림 조회 (최신순). 최근 N개가 필요하면 PageRequest.of(0, N) 을 넘긴다.
  Page<Notification> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

  // 사용자의 읽지 않은 알림 조회
  List<Notification> findByUserAndIsReadFalseOrderByCreatedAtDesc(User user);

  Page<Notification> findByUserAndIsReadFalseOrderByCreatedAtDesc(User user, Pageable pageable);

  // 사용자의 읽지 않은 알림 개수
  Long countByUserAndIsReadFalse(User user);

  // 보관 한도를 넘겼는지 확인할 때 쓴다. OFFSET N 으로 (N+1)번째 알림만 집어 온다.
  List<Notification> findByUser_IdOrderByIdDesc(Long userId, Pageable pageable);

  // 기준선보다 오래된 알림을 한 번에 지운다
  long deleteByUser_IdAndIdLessThanEqual(Long userId, Long id);

}

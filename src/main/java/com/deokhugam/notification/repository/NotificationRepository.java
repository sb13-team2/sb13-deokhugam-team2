package com.deokhugam.notification.repository;

import com.deokhugam.notification.entity.Notification;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {

  @Modifying
  @Query("delete from Notification n where n.review.id = :reviewId")
  void deleteAllByReviewId(@Param("reviewId") UUID reviewId);

  // 유저가 받은 알림 전체 삭제를 위해 추가
  void deleteAllByUserId(UUID userId);

  //user의 안읽은(isConfirmed=false) 알림만 모두 가져오는 퀴리 메서드
  List<Notification> findAllByUserIdAndIsConfirmedFalse(UUID userId);

  // 1. 내 알림만 카운트하는 메서드 (리뷰 피드백 반영)
  long countByUserId(UUID userId);
  // 2. 내림차순 정렬 (최신순) - 과거 시간(<) 탐색
  @Query("select n from Notification n where n.user.id = :userId " +
      "and (:cursor is null or n.createdAt < :cursor) " +
      "order by n.createdAt desc")
  List<Notification> findAllByCursorDesc(
      @Param("userId") UUID userId,
      @Param("cursor") LocalDateTime cursor,
      Pageable pageable
  );
  // 3. 오름차순 정렬 (오래된순) - 미래 시간(>) 탐색
  @Query("select n from Notification n where n.user.id = :userId " +
      "and (:cursor is null or n.createdAt > :cursor) " +
      "order by n.createdAt asc")
  List<Notification> findAllByCursorAsc(
      @Param("userId") UUID userId,
      @Param("cursor") LocalDateTime cursor,
      Pageable pageable
  );

  // 특정 날짜(cutoffDate)보다 이전에 생성된 알림들을 한 방에 삭제하는 쿼리
  @Modifying
  @Query("delete from Notification n where n.isConfirmed = true and n.confirmedAt < :cutoffDate")
  int deleteOldConfirmedNotifications(@Param("cutoffDate") LocalDateTime cutoffDate);
}

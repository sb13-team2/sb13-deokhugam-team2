package com.deokhugam.notification.service;

import com.deokhugam.global.exception.DeokhugamException;
import com.deokhugam.global.exception.ErrorCode;
import com.deokhugam.notification.dto.request.NotificationUpdateRequest;
import com.deokhugam.notification.dto.response.NotificationDto;
import com.deokhugam.notification.dto.response.NotificationListResponse;
import com.deokhugam.notification.entity.Notification;
import com.deokhugam.notification.entity.NotificationType;
import com.deokhugam.notification.repository.NotificationRepository;
import com.deokhugam.review.entity.Review;
import com.deokhugam.review.repository.ReviewRepository;
import com.deokhugam.user.entity.User;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationService {

  private final NotificationRepository notificationRepository;
  private final ReviewRepository reviewRepository;

  @Transactional
  public NotificationDto readNotification(UUID notificationId, UUID userId, NotificationUpdateRequest request) {
    Notification notification = notificationRepository.findById(notificationId)
        .orElseThrow(()-> new DeokhugamException(ErrorCode.NOTIFICATION_NOT_FOUND));

    if (!notification.getUser().getId().equals(userId)) {
      throw new DeokhugamException(ErrorCode.FORBIDDEN); // 403
    }

    notification.updateConfirmStatus(request.confirmed());

    return new NotificationDto(
        notification.getId(),
        notification.getUser().getId(),
        notification.getReview().getId(),
        notification.getReview().getContent(),
        notification.getContent(),
        notification.isConfirmed(),
        notification.getConfirmedAt(),
        notification.getCreatedAt(),
        notification.getUpdatedAt(),
        notification.getType()
    );
  }

  @Transactional
  public void readAllNotification(UUID userId){
    List<Notification> unreadNotification = notificationRepository.findAllByUserIdAndIsConfirmedFalse(userId);

    // 가져온 알림들을 하나씩 꺼내어 무조건 읽음(true)으로 바꿉니다.
    for(Notification notifications : unreadNotification) {
      notifications.updateConfirmStatus(true);
    }
  }

  @Transactional(readOnly = true)
  public NotificationListResponse getNotifications(UUID userId, String direction, String cursor, LocalDateTime after, int limit) {

    // 1. 다음 페이지가 있는지 확인하기 위해 일부러 limit보다 1개 더(+1) 많이 가져옵니다.
    PageRequest pageRequest = PageRequest.of(0, limit + 1);

    // 2. 커서 처리: after가 있으면 최우선 적용, 없으면 문자열 cursor를 변환
    LocalDateTime targetCursor = null;
    if (after != null) {
      targetCursor = after;
    } else if (cursor != null && !cursor.isBlank()) {
      targetCursor = LocalDateTime.parse(cursor);
    }

    // 3. 정렬 방향(direction)에 따라 다른 쿼리 호출
    List<Notification> searched;
    if ("ASC".equalsIgnoreCase(direction)) {
      searched = notificationRepository.findAllByCursorAsc(userId, targetCursor, pageRequest);
    } else {
      searched = notificationRepository.findAllByCursorDesc(userId, targetCursor, pageRequest);
    }

    // 4. limit보다 1개 더 많이 조회되었다면, 다음 페이지가 있다는 뜻!
    boolean hasNext = searched.size() > limit;

    // 5. 진짜 프론트엔드에 줄 데이터(limit 개수만큼만 자름)
    List<Notification> notifications = hasNext ? searched.subList(0, limit) : searched;

    // 6. Entity -> DTO 변환
    List<NotificationDto> content = notifications.stream()
        .map(notification -> new NotificationDto(
            notification.getId(), notification.getUser().getId(),
            notification.getReview().getId(), notification.getReview().getContent(),
            notification.getContent(), notification.isConfirmed(),
            notification.getConfirmedAt(), notification.getCreatedAt(),
            notification.getUpdatedAt(), notification.getType()
        )).toList();

    // 7. 다음 페이지 요청을 위한 커서값 세팅
    String nextCursor = null;
    LocalDateTime nextAfter = null;
    if (hasNext && !notifications.isEmpty()) {
      Notification lastItem = notifications.get(notifications.size() - 1);
      nextCursor = lastItem.getCreatedAt() != null ? lastItem.getCreatedAt().toString() : null;
      nextAfter = lastItem.getCreatedAt();
    }

    // 8. 총 개수 (추후 최적화 가능하지만 일단 DB 카운트 사용)
    long totalElements = notificationRepository.countByUserId(userId);

    return new NotificationListResponse(
        content, nextCursor, nextAfter, content.size(), totalElements, hasNext
    );
  }

  @Transactional
  public NotificationDto createNotification(User receiver, Review review, String content, NotificationType type) {

    // 1. 알림 엔티티 생성 (Builder 사용, 초기 상태는 무조건 isConfirmed = false)
    Notification notification = Notification.builder()
        .user(receiver) // 알림을 받을 사람 (예: 리뷰 작성자)
        .review(review) // 어떤 리뷰에서 발생했는지
        .content(content) // 알림 메시지 ("OOO님이 좋아요를...")
        .type(type)
        .build();
    // 2. DB에 저장
    Notification savedNotification = notificationRepository.save(notification);
    // 3. 응답용 DTO로 변환하여 반환
    return new NotificationDto(
        savedNotification.getId(),
        savedNotification.getUser().getId(),
        savedNotification.getReview().getId(),
        savedNotification.getReview().getContent(),
        savedNotification.getContent(),
        savedNotification.isConfirmed(),
        savedNotification.getConfirmedAt(),
        savedNotification.getCreatedAt(),
        savedNotification.getUpdatedAt(),
        savedNotification.getType()
    );
  }

  // 대시보드에서 1~10위 리뷰 ID들을 주면, 해당 리뷰 작성자들에게 알림을 생성.
  @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
  public void createTopReviewNotifications(List<UUID> topReviewIds) {
    // 1. 리뷰 ID들로 리뷰 엔티티들을 DB에서 한 번에 싹 다 가져옵니다.
    List<Review> topReviews = reviewRepository.findAllById(topReviewIds);

    // 2. 알림 엔티티들을 담을 빈 리스트를 만듭니다.
    List<Notification> notificationsToSave = new ArrayList<>();

    // 3. 가져온 리뷰들을 for문으로 돌면서 알림(Notification) 객체를 만들고 리스트에 넣습니다.
    for (Review review : topReviews) {
      Notification notification = Notification.builder()
          .user(review.getUser()) // 리뷰 작성자에게 알림을 보냅니다!
          .review(review)
          .content("축하합니다! 작성하신 리뷰가 인기 TOP 리뷰에 선정되었습니다! 🎉")
          .type(NotificationType.TOP_REVIEW)
          .build();
      notificationsToSave.add(notification);
    }

    // 4. 만들어진 10개의 알림을 DB에 한 방에 저장!
    notificationRepository.saveAll(notificationsToSave);
  }
}

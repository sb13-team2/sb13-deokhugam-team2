package com.deokhugam.notification.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import com.deokhugam.global.exception.DeokhugamException;
import com.deokhugam.notifications.dto.request.NotificationUpdateRequest;
import com.deokhugam.notifications.dto.response.NotificationDto;
import com.deokhugam.notifications.entity.Notification;
import com.deokhugam.notifications.entity.NotificationType;
import com.deokhugam.notifications.repository.NotificationRepository;
import com.deokhugam.notifications.service.NotificationService;
import com.deokhugam.review.entity.Review;
import com.deokhugam.user.entity.User;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class NotificationServiceTest {

  @Mock
  private NotificationRepository notificationRepository;

  @InjectMocks
  private NotificationService notificationService;

  // 공통된 가짜(Mock) 객체들을 생성하는 헬퍼 메서드
  private Notification createMockNotification(UUID userId, UUID reviewId) {
    User mockUser = mock(User.class);
    given(mockUser.getId()).willReturn(userId);
    Review mockReview = mock(Review.class);
    given(mockReview.getId()).willReturn(reviewId);
    // [중요] Review 엔티티에 getContent()가 생겼으므로 가짜 내용 세팅!
    given(mockReview.getContent()).willReturn("재밌는 책이네요");
    return Notification.builder()
        .content("테스트 알림 메시지")
        .type(NotificationType.REVIEW_LIKE)
        .user(mockUser)
        .review(mockReview)
        .build();
  }

  @Test
  @DisplayName("알림 읽음 처리 성공 - true 요청 시 상태가 true가 되고 시간이 기록된다")
  void updateNotification_Success_True() {
    // given
    UUID userId = UUID.randomUUID();
    UUID notificationId = UUID.randomUUID();
    Notification mockNotification = createMockNotification(userId, UUID.randomUUID());

    given(notificationRepository.findById(notificationId))
        .willReturn(Optional.of(mockNotification));

    NotificationUpdateRequest requestDto = new NotificationUpdateRequest(true);

    // when
    NotificationDto result = notificationService.readNotification(notificationId, userId, requestDto);

    // then
    assertThat(result.confirmed()).isTrue();
    assertThat(result.confirmedAt()).isNotNull();

    // DTO 필드명 변경된 부분들도 잘 들어갔는지 검증!
    assertThat(result.reviewContent()).isEqualTo("재밌는 책이네요");
    assertThat(result.message()).isEqualTo("테스트 알림 메시지");
  }

  @Test
  @DisplayName("알림 안 읽음 처리 성공 - false 요청 시 상태가 false가 되고 시간이 null이 된다")
  void updateNotification_Success_False() {
    // given
    UUID userId = UUID.randomUUID();
    UUID notificationId = UUID.randomUUID();
    Notification mockNotification = createMockNotification(userId, UUID.randomUUID());

    given(notificationRepository.findById(notificationId))
        .willReturn(Optional.of(mockNotification));

    // 1. 먼저 읽음(true) 처리
    notificationService.readNotification(notificationId, userId, new NotificationUpdateRequest(true));

    // 2. 다시 안 읽음(false) 상자를 만들어서 전송
    NotificationUpdateRequest requestDto = new NotificationUpdateRequest(false);

    // when
    NotificationDto result = notificationService.readNotification(notificationId, userId, requestDto);

    // then
    assertThat(result.confirmed()).isFalse();
    assertThat(result.confirmedAt()).isNull(); // 시간이 다시 지워져야 함!
  }
  @Test
  @DisplayName("타인의 알림 접근 시 FORBIDDEN(403) 예외가 발생한다")
  void updateNotification_Fail_Forbidden() {
    // given
    UUID ownerId = UUID.randomUUID();
    UUID requesterId = UUID.randomUUID(); // 다른 사용자!
    UUID notificationId = UUID.randomUUID();

    Notification mockNotification = createMockNotification(ownerId, UUID.randomUUID());
    given(notificationRepository.findById(notificationId))
        .willReturn(Optional.of(mockNotification));
    NotificationUpdateRequest requestDto = new NotificationUpdateRequest(true);

    // when & then
    assertThatThrownBy(() -> notificationService.readNotification(notificationId, requesterId, requestDto))
        .isInstanceOf(DeokhugamException.class)
        .hasMessageContaining("권한이 없습니다"); // ErrorCode.FORBIDDEN의 메시지
  }
}

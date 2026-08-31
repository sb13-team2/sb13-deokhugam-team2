package com.deokhugam.notification.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import com.deokhugam.global.exception.DeokhugamException;
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
import java.util.List;
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
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class NotificationServiceTest {

  @Mock
  private NotificationRepository notificationRepository;

  @Mock
  private ReviewRepository reviewRepository;

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
    NotificationDto result = notificationService.readNotification(notificationId, userId,
        requestDto);

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
    notificationService.readNotification(notificationId, userId,
        new NotificationUpdateRequest(true));

    // 2. 다시 안 읽음(false) 상자를 만들어서 전송
    NotificationUpdateRequest requestDto = new NotificationUpdateRequest(false);

    // when
    NotificationDto result = notificationService.readNotification(notificationId, userId,
        requestDto);

    // then
    assertThat(result.confirmed()).isFalse();
    assertThat(result.confirmedAt()).isNull(); // 시간이 다시 지워져야 함!
  }

  @Test
  @DisplayName("타인의 알림 접근 시 FORBIDDEN(403) 예외가 발생한다")
  void updateNotification_Fail_Forbidden() {
    // given
    UUID ownerId = UUID.randomUUID();
    UUID requesterId = UUID.randomUUID(); // 다른 사용자
    UUID notificationId = UUID.randomUUID();

    Notification mockNotification = createMockNotification(ownerId, UUID.randomUUID());
    given(notificationRepository.findById(notificationId))
        .willReturn(Optional.of(mockNotification));
    NotificationUpdateRequest requestDto = new NotificationUpdateRequest(true);

    // when & then
    assertThatThrownBy(
        () -> notificationService.readNotification(notificationId, requesterId, requestDto))
        .isInstanceOf(DeokhugamException.class)
        .hasMessageContaining("권한이 없습니다"); // ErrorCode.FORBIDDEN의 메시지
  }

  @Test
  @DisplayName("새로운 알림 생성 시 DB에 저장되고 DTO를 반환한다")
  void createNotification_Success() {
    // given
    UUID ownerId = UUID.randomUUID();
    UUID reviewId = UUID.randomUUID();

    // 유저와 리뷰 정보만 빼오기 위해 헬퍼 사용
    Notification tempNotification = createMockNotification(ownerId, reviewId);

    // [수정된 부분] DB에서 튀어나올 결과물을 우리가 기대하는 메시지로 직접 조립합니다.
    Notification expectedNotification = Notification.builder()
        .user(tempNotification.getUser())
        .review(tempNotification.getReview())
        .content("OOO님이 좋아요를 눌렀습니다.") // 기대하는 메시지로 세팅!
        .type(NotificationType.REVIEW_LIKE)
        .build();
    // DB에 save()를 하면 expectedNotification이 튀어나온다고 설정
    given(notificationRepository.save(any(Notification.class)))
        .willReturn(expectedNotification);
    // when
    NotificationDto result = notificationService.createNotification(
        tempNotification.getUser(),
        tempNotification.getReview(),
        "OOO님이 좋아요를 눌렀습니다.",
        NotificationType.REVIEW_LIKE
    );
    // then
    assertThat(result.message()).isEqualTo("OOO님이 좋아요를 눌렀습니다.");
    assertThat(result.confirmed()).isFalse();
  }

  @Test
  @DisplayName("전체 알림 읽음 처리 시 안 읽은 알림들이 모두 읽음 상태로 변경된다")
  void readAllNotification_Success() {
    // given
    UUID userId = UUID.randomUUID();
    Notification noti1 = createMockNotification(userId, UUID.randomUUID());
    Notification noti2 = createMockNotification(userId, UUID.randomUUID());

    // Repository가 안 읽은 알림 2개를 반환한다고 가짜 설정
    given(notificationRepository.findAllByUserIdAndIsConfirmedFalse(userId))
        .willReturn(List.of(noti1, noti2));
    // when
    notificationService.readAllNotification(userId);
    // then
    assertThat(noti1.isConfirmed()).isTrue();
    assertThat(noti2.isConfirmed()).isTrue();
  }
  @Test
  @DisplayName("알림 목록 조회 시 다음 커서 정보와 함께 알림 목록이 반환된다")
  void getNotifications_Success() {
    // given
    UUID userId = UUID.randomUUID();
    Notification noti1 = createMockNotification(userId, UUID.randomUUID());
    Notification noti2 = createMockNotification(userId, UUID.randomUUID());

    // 테스트용 생성 시간(CreatedAt)을 강제로 세팅 (BaseEntity라 setter가 없으므로 Reflection 사용)
    ReflectionTestUtils.setField(noti1, "createdAt", LocalDateTime.now().minusHours(1));
    ReflectionTestUtils.setField(noti2, "createdAt", LocalDateTime.now());

    // Repository가 limit+1 개인 2개를 조회했다고 가짜 설정
    given(notificationRepository.findAllByCursorDesc(any(), any(), any()))
        .willReturn(List.of(noti2, noti1)); // 최신순(noti2가 먼저) 정렬
    //사용자의 전체 알림 개수를 2개로 가정
    given(notificationRepository.countByUserId(userId)).willReturn(2L); // 최신순(noti2가 먼저) 정렬

    // when (limit을 1로 요청 -> 2개가 조회됐으니 hasNext는 true여야 함!)
    NotificationListResponse result = notificationService.getNotifications(userId, "DESC", null, null, 1);

    // then
    assertThat(result.hasNext()).isTrue();
    assertThat(result.content()).hasSize(1);
    assertThat(result.nextCursor()).isNotNull();
  }

  @Test
  @DisplayName("대시보드 탑 10 리뷰 ID 리스트를 받으면 다건의 알림이 생성 및 저장되어야 한다")
  void createTopReviewNotifications_Success() {
    // 1. given (가짜 데이터 준비)
    // 테스트니까 10개 대신 2개의 리뷰 ID만 넘어왔다고 가정합니다.
    UUID reviewId1 = UUID.randomUUID();
    UUID reviewId2 = UUID.randomUUID();
    List<UUID> topReviewIds = List.of(reviewId1, reviewId2);

    // 가짜 유저와 가짜 리뷰 세팅 (Mockito 사용)
    User mockUser = org.mockito.Mockito.mock(User.class);
    Review mockReview1 = org.mockito.Mockito.mock(Review.class);
    org.mockito.BDDMockito.given(mockReview1.getUser()).willReturn(mockUser);
    Review mockReview2 = org.mockito.Mockito.mock(Review.class);
    org.mockito.BDDMockito.given(mockReview2.getUser()).willReturn(mockUser);

    org.mockito.BDDMockito.given(reviewRepository.findAllById(org.mockito.ArgumentMatchers.any()))
        .willReturn(List.of(mockReview1, mockReview2));

    // 2. when (실제 동작)
    notificationService.createTopReviewNotifications(topReviewIds);

    // 3. then (결과 검증)
    // notificationRepository.saveAll() 에 전달된 List<Notification> 파라미터를 낚아채는 도구 준비
    @SuppressWarnings("unchecked")
    org.mockito.ArgumentCaptor<List<Notification>> captor = org.mockito.ArgumentCaptor.forClass(List.class);
    //notificationRepository의 saveAll 메서드가 정확히 1번 호출되었는지 검증(verify)하며 값을 낚아채도록 합니다.
    org.mockito.Mockito.verify(notificationRepository, org.mockito.Mockito.times(1)).saveAll(captor.capture());
    // 낚아챈 알림 리스트 꺼내기
    List<Notification> savedNotifications = captor.getValue();
    // 검증 1: 낚아챈 알림 리스트의 개수는 우리가 준비한 가짜 리뷰 개수인 2개여야 합니다!
    org.assertj.core.api.Assertions.assertThat(savedNotifications).hasSize(2);
    // 검증 2: 만들어진 첫 번째 알림의 타입(Type)은 TOP_REVIEW 여야 합니다!
    org.assertj.core.api.Assertions.assertThat(savedNotifications.get(0).getType())
        .isEqualTo(NotificationType.TOP_REVIEW);
  }
}
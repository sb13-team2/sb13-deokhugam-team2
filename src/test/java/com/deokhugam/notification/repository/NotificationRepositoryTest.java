package com.deokhugam.notification.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.deokhugam.book.entity.Book;
import com.deokhugam.notifications.entity.Notification;
import com.deokhugam.notifications.entity.NotificationType;
import com.deokhugam.notifications.repository.NotificationRepository;
import com.deokhugam.review.entity.Review;
import com.deokhugam.user.entity.User;
import com.deokhugam.global.config.JpaConfig;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import(JpaConfig.class)
class NotificationRepositoryTest {

  @Autowired
  private NotificationRepository notificationRepository;

  @Autowired
  private EntityManager entityManager; //JPA 영속성 컨텍스트를 직접 비우기 위해 사용됨.

  @Autowired
  private com.deokhugam.user.repository.UserRepository userRepository;

  @Autowired
  private com.deokhugam.book.repository.BookRepository bookRepository;

  @Autowired
  private com.deokhugam.review.repository.ReviewRepository reviewRepository;

  @Test
  @DisplayName("리뷰 ID로 해당 리뷰의 알림을 모두 물리 삭제하고 다른 리뷰의 알림은 유지한다")
  void deleteAllByReviewId() {

    // given (테스트용 데이터)
    User user = userRepository.save(
        User.create(
            "notification-test@example.com",
            "notificationUser",
            "password"
        )
    );

    Book bookA = bookRepository.save(
        new Book(
            "도서 A",
            "저자 A",
            "설명 A",
            "출판사 A",
            LocalDate.of(2026, 1, 1),
            "9781234567891"
        )
    );

    Book bookB = bookRepository.save(
        new Book(
            "도서 B",
            "저자 B",
            "설명 B",
            "출판사 B",
            LocalDate.of(2026, 1, 2),
            "9781234567892"
        )
    );

    Review reviewA = reviewRepository.save(
        Review.create(user, bookA, "리뷰 A", 5)
    );

    Review reviewB = reviewRepository.save(
        Review.create(user, bookB, "리뷰 B", 4)
    );

    Notification notificationA1 = Notification.builder()
        .user(user)
        .review(reviewA)
        .content("리뷰 A 알림 1")
        .type(NotificationType.REVIEW_LIKE)
        .build();

    Notification notificationA2 = Notification.builder()
        .user(user)
        .review(reviewA)
        .content("리뷰 A 알림 2")
        .type(NotificationType.NEW_COMMENT)
        .build();

    Notification notificationB = Notification.builder()
        .user(user)
        .review(reviewB)
        .content("리뷰 B 알림")
        .type(NotificationType.REVIEW_LIKE)
        .build();

    notificationRepository.saveAll(
        List.of(notificationA1, notificationA2, notificationB)
    );
    notificationRepository.flush();

    // when
    notificationRepository.deleteAllByReviewId(reviewA.getId());
    notificationRepository.flush();

    entityManager.clear();

    // then
    List<Notification> remaining =
        notificationRepository.findAll();

    assertThat(remaining)
        .hasSize(1);

    assertThat(remaining.get(0).getReview().getId())
        .isEqualTo(reviewB.getId());

    assertThat(remaining)
        .noneMatch(notification ->
            notification.getReview().getId().equals(reviewA.getId()));
  }
}
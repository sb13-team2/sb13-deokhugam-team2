package com.deokhugam.notifications.entity;

import com.deokhugam.global.entity.BaseEntity;
import com.deokhugam.review.entity.Review;
import com.deokhugam.user.entity.User;
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
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "notifications")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(columnDefinition = "UUID")
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "review_id", nullable = false)
  private Review review;

  @Column(nullable = false, length = 500)
  private String content;

  private LocalDateTime confirmedAt;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  private NotificationType type;

  @Column(nullable = false)
  private boolean isConfirmed = false;

  @Builder
  public Notification(User user, Review review, String content, NotificationType type) {
    this.user = user;
    this.review = review;
    this.content = content;
    this.type = type;
    this.isConfirmed = false;
  }

  // 객체지향적인 비즈니스 메서드 추가(알림 읽음 처리용)
  public void updateConfirmStatus(boolean confirmed) {
    if (this.isConfirmed == confirmed) return; //이미 읽었을 경우 다음 읽어도 무시
    this.isConfirmed = confirmed; // 매개변수로 넘어온 값으로 변경
    this.confirmedAt = confirmed ? LocalDateTime.now() : null; //true면 현재시간, false면 null
  }
}

package com.deokhugam.notification.event;

import com.deokhugam.dashboard.event.TopReviewRankedEvent;
import com.deokhugam.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class NotificationEventListener {

  private final NotificationService notificationService;

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handleTopReviewEvent(TopReviewRankedEvent event) {
    // 이벤트 봉투에서 ID 리스트를 꺼내서 알림 생성 서비스로 넘깁니다.
    notificationService.createTopReviewNotifications(event.topReviewIds());
  }

}

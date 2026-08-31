package com.deokhugam.notification.batch;

import com.deokhugam.notification.repository.NotificationRepository;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class NotificationCleanupBatch {

  private final NotificationRepository notificationRepository;

  @Transactional
  //@Scheduled(cron = "*/10 * * * * *") //테스트용
  @Scheduled(cron = "0 0 4 * * *", zone = "Asia/Seoul")
  public void cleanupOldNotifications() {
    LocalDateTime cutoffDate = LocalDateTime.now().minusDays(7);
    int deleteCount = notificationRepository.deleteOldConfirmedNotifications(cutoffDate);
    log.info("오래된 알림 {}건 삭제 완료", deleteCount);
  }
}

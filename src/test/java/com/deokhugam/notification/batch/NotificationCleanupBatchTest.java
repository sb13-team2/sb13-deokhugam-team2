package com.deokhugam.notification.batch;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.deokhugam.notification.repository.NotificationRepository;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NotificationCleanupBatchTest {

  @Mock
  private NotificationRepository notificationRepository;

  @InjectMocks
  private NotificationCleanupBatch batch;

  @Test
  @DisplayName("7일 지난 읽음 처리된 알림 삭제 쿼리가 정상적으로 호출되어야 한다")
  void cleanupOldNotifications_Success() {
    // given: Repository가 5개를 지웠다고 흉내 냅니다.
    given(notificationRepository.deleteOldConfirmedNotifications(any())).willReturn(5);

    // when: 스케줄러 메서드를 강제로 실행해 봅니다.
    batch.cleanupOldNotifications();

    // then: Repository의 삭제 메서드가 딱 1번 잘 호출되었는지 검증합니다!
    // ArgumentCaptor를 사용해서 메서드 파라미터를 추출
    org.mockito.ArgumentCaptor<LocalDateTime> captor = org.mockito.ArgumentCaptor.forClass(
        LocalDateTime.class);
    verify(notificationRepository, times(1)).deleteOldConfirmedNotifications(captor.capture());

    // 추출한 값(captor.getValue())이 7일 전 날짜인지 검증 (오차 고려해서 날짜만 맞는지 체크)
    org.assertj.core.api.Assertions.assertThat(captor.getValue().toLocalDate())
        .isEqualTo(LocalDateTime.now().minusDays(7).toLocalDate());
  }
}
package com.deokhugam.notification.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import com.deokhugam.notification.service.NotificationService;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(NotificationController.class)
@AutoConfigureMockMvc(addFilters = false) // (시큐리티 검사 무시)
class NotificationControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private NotificationService notificationService;

  @Test
  @DisplayName("사용자의 모든 알림을 읽음 처리하면 200 OK를 반환한다")
  void readAllNotification_Success() throws Exception {
    // given
    UUID userId = UUID.randomUUID();

    // when & then
    mockMvc.perform(patch("/api/notifications/read-all") // Swagger 주소로 맞춤 (다르면 수정 요망)
            .header("Deokhugam-Request-User-ID", userId.toString()))
        .andExpect(status().isNoContent());

    // Service 메서드가 1번 불렸는지 검증
    verify(notificationService).readAllNotification(userId);
  }

  @Test
  @DisplayName("요청자 ID 헤더가 없으면 400 Bad Request를 반환한다")
  void readAllNotification_WithoutUserIdHeader_BadRequest() throws Exception {
    // when & then (헤더 없이 요청)
    mockMvc.perform(patch("/api/notifications/read-all"))
        .andExpect(status().isBadRequest());

    // 헤더가 없으면 Service까지 도달하면 안 됨! (never)
    verify(notificationService, never()).readAllNotification(org.mockito.ArgumentMatchers.any());
  }

  @Test
  @DisplayName("사용자의 알림 목록을 커서 기반으로 조회하면 200 OK를 반환한다")
  void getNotifications_Success() throws Exception {
    // given
    UUID userId = UUID.randomUUID();
    String after = "2026-08-25T10:00:00"; // 기준이 되는 마지막 알림의 시간
    int limit = 10; // 한 번에 가져올 개수
    // when & then
    // GET /api/notification?cursor=...&size=10
    mockMvc.perform(get("/api/notifications")
            .param("userId", userId.toString())
            .param("after", after)
            .param("limit", String.valueOf(limit)))
        .andExpect(status().isOk());
  }

}
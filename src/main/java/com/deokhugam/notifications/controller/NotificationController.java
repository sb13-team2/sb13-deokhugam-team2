package com.deokhugam.notifications.controller;

import com.deokhugam.notifications.dto.request.NotificationUpdateRequest;
import com.deokhugam.notifications.dto.response.NotificationDto;
import com.deokhugam.notifications.service.NotificationService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

  private final NotificationService notificationService;

  @PatchMapping("/{notificationId}")
  public ResponseEntity<NotificationDto> readNotification(
      @PathVariable UUID notificationId,
      @RequestHeader("Deokhugam-Request-User-ID")UUID userId,
      @RequestBody NotificationUpdateRequest request
      ) {
    NotificationDto result = notificationService.readNotification(notificationId, userId, request);
    return ResponseEntity.ok(result);
  }
}

package com.deokhugam.notifications.service;

import com.deokhugam.global.exception.DeokhugamException;
import com.deokhugam.global.exception.ErrorCode;
import com.deokhugam.notifications.dto.request.NotificationUpdateRequest;
import com.deokhugam.notifications.dto.response.NotificationDto;
import com.deokhugam.notifications.entity.Notification;
import com.deokhugam.notifications.repository.NotificationRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationService {

  private final NotificationRepository notificationRepository;

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

}

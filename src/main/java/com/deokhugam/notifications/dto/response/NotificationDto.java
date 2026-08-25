package com.deokhugam.notifications.dto.response;

import com.deokhugam.notifications.entity.NotificationType;
import java.time.LocalDateTime;
import java.util.UUID;

public record NotificationDto(
    UUID id,
    UUID userId,
    UUID reviewId,
    String reviewContent,
    String message,
    boolean confirmed,
    LocalDateTime confirmedAt,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    NotificationType type
) { }

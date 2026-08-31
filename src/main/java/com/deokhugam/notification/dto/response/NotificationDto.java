package com.deokhugam.notification.dto.response;

import com.deokhugam.notification.entity.NotificationType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.UUID;

public record NotificationDto(
    UUID id,
    UUID userId,
    UUID reviewId,
    String reviewContent,
    String message,
    boolean confirmed,

    @Schema(hidden = true)
    LocalDateTime confirmedAt,

    LocalDateTime createdAt,
    LocalDateTime updatedAt,

    @Schema(hidden = true)
    NotificationType type
) { }

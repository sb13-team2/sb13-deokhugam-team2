package com.deokhugam.notification.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;

public record NotificationListResponse(
    List<NotificationDto> content, // 실제 알림 목록
    String nextCursor,             // 다음 커서 (시간 문자열)
    @Schema(example = "2025-04-06T15:04:05.000Z")
    LocalDateTime nextAfter,       // 다음 커서 (시간 객체)
    @Schema(example = "10")
    int size,                      // 현재 페이지의 데이터 개수
    @Schema(example = "100")
    long totalElements,            // 전체 알림 개수
    boolean hasNext                // 다음 페이지 존재 여부
) { }

package com.deokhugam.user.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record PowerUserDto(
        UUID userId,
        String nickname,
        String period,
        LocalDateTime createdAt,
        Long rank,
        Double score,
        Double reviewScoreSum,
        Long likeCount,
        Long commentCount
) {}
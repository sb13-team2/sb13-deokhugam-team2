package com.deokhugam.user.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record CursorPageResponsePowerUserDto(
        List<PowerUserDto> content,
        String nextCursor,
        LocalDateTime nextAfter,
        int size,
        long totalElements,
        boolean hasNext
) {}
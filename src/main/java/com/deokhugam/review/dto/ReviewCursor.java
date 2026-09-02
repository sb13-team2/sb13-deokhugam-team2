package com.deokhugam.review.dto;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

public record ReviewCursor(
        String sortValue,
        UUID reviewId
) {

    private static final String DELIMITER = "|";

    public static String encode(
            Object sortValue,
            UUID reviewId
    ) {
        String payload = sortValue + DELIMITER + reviewId;

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(
                        payload.getBytes(StandardCharsets.UTF_8)
                );
    }

    public static ReviewCursor decode(String cursor) {
        try {
            String payload = new String(
                    Base64.getUrlDecoder().decode(cursor),
                    StandardCharsets.UTF_8
            );
            int delimiterIndex = payload.lastIndexOf(DELIMITER);

            if (delimiterIndex <= 0
                    || delimiterIndex == payload.length() - 1) {
                throw new IllegalArgumentException(
                        "리뷰 커서 형식이 올바르지 않습니다."
                );
            }

            return new ReviewCursor(
                    payload.substring(0, delimiterIndex),
                    UUID.fromString(
                            payload.substring(delimiterIndex + 1)
                    )
            );
        } catch (RuntimeException exception) {
            throw new IllegalArgumentException(
                    "리뷰 커서 형식이 올바르지 않습니다.",
                    exception
            );
        }
    }
}
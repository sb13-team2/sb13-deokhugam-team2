package com.deokhugam.review.exception;

import com.deokhugam.global.exception.DeokhugamException;
import com.deokhugam.global.exception.ErrorCode;
import java.util.Map;
import java.util.UUID;

public class ReviewAccessDeniedException extends DeokhugamException {

    public ReviewAccessDeniedException(
            UUID reviewId,
            UUID requesterId
    ) {
        super(
                ErrorCode.REVIEW_ACCESS_DENIED,
                Map.of(
                        "reviewId", reviewId,
                        "requesterId", requesterId
                )
        );
    }
}
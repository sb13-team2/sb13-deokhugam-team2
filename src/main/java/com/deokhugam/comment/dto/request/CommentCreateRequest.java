package com.deokhugam.comment.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record CommentCreateRequest(

        @NotNull
        UUID userId,

        @NotNull
        UUID reviewId,

        @NotBlank
        @Size(max = 1000)
        String content
) {
}
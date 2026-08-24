package com.deokhugam.comment.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CommentUpdateRequest(

        @NotBlank
        @Size(max = 1000)
        String content
) {
}
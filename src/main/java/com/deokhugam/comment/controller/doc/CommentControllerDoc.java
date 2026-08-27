package com.deokhugam.comment.controller.doc;

import com.deokhugam.comment.dto.request.CommentCreateRequest;
import com.deokhugam.comment.dto.request.CommentSearchRequest;
import com.deokhugam.comment.dto.request.CommentUpdateRequest;
import com.deokhugam.comment.dto.response.CommentListResponse;
import com.deokhugam.comment.dto.response.CommentResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@Tag(
        name = "Comment",
        description = "댓글 관리 API"
)
public interface CommentControllerDoc {

    @Operation(
            summary = "댓글 등록",
            description = "리뷰에 새로운 댓글을 등록합니다."
    )
    ResponseEntity<CommentResponse> create(
            @Valid
            @RequestBody
            CommentCreateRequest request
    );

    @Operation(
            summary = "댓글 수정",
            description = "댓글 작성자가 자신의 댓글 내용을 수정합니다."
    )
    ResponseEntity<CommentResponse> update(
            @Parameter(
                    description = "수정할 댓글 ID",
                    required = true
            )
            @PathVariable
            UUID commentId,

            @Parameter(
                    description = "요청 사용자 ID",
                    required = true
            )
            @RequestHeader("Deokhugam-Request-User-ID")
            UUID requesterId,

            @Valid
            @RequestBody
            CommentUpdateRequest request
    );

    @Operation(
            summary = "댓글 삭제",
            description = "댓글 작성자가 자신의 댓글을 논리 삭제합니다."
    )
    ResponseEntity<Void> delete(
            @Parameter(
                    description = "삭제할 댓글 ID",
                    required = true
            )
            @PathVariable
            UUID commentId,

            @Parameter(
                    description = "요청 사용자 ID",
                    required = true
            )
            @RequestHeader("Deokhugam-Request-User-ID")
            UUID requesterId
    );

    @Operation(
            summary = "댓글 목록 조회",
            description = "조건에 따라 댓글 목록을 커서 기반 페이지네이션으로 조회합니다."
    )
    ResponseEntity<CommentListResponse> findAll(
            @Valid
            @ModelAttribute
            CommentSearchRequest request
    );
}
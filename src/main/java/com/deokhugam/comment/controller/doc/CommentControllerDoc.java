package com.deokhugam.comment.controller.doc;

import com.deokhugam.comment.dto.request.CommentCreateRequest;
import com.deokhugam.comment.dto.request.CommentSearchRequest;
import com.deokhugam.comment.dto.request.CommentUpdateRequest;
import com.deokhugam.comment.dto.response.CommentListResponse;
import com.deokhugam.comment.dto.response.CommentResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@Tag(
        name = "댓글 관리",
        description = "댓글 관련 API"
)
public interface CommentControllerDoc {

    @Operation(
            summary = "댓글 등록",
            description = "리뷰에 새로운 댓글을 등록합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "댓글 등록 성공"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (입력값 검증 실패)"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "사용자 또는 리뷰를 찾을 수 없음"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "서버 내부 오류"
            )
    })
    ResponseEntity<CommentResponse> create(
            @RequestBody CommentCreateRequest request
    );

    @Operation(
            summary = "댓글 수정",
            description = "작성자 본인이 댓글 내용을 수정합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "댓글 수정 성공"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (입력값 검증 실패)"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "댓글 수정 권한 없음"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "댓글을 찾을 수 없음"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "서버 내부 오류"
            )
    })
    ResponseEntity<CommentResponse> update(
            @PathVariable UUID commentId,
            @RequestHeader("Deokhugam-Request-User-ID")
            UUID requesterId,
            @RequestBody CommentUpdateRequest request
    );

    @Operation(
            summary = "댓글 삭제",
            description = "작성자 본인이 댓글을 논리적으로 삭제합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "댓글 삭제 성공"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 또는 요청 사용자 ID 헤더 누락"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "댓글 삭제 권한 없음"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "댓글을 찾을 수 없음"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "서버 내부 오류"
            )
    })
    ResponseEntity<Void> delete(
            @PathVariable UUID commentId,
            @RequestHeader("Deokhugam-Request-User-ID")
            UUID requesterId
    );

    @Operation(
            summary = "댓글 목록 조회",
            description = "리뷰의 댓글 목록을 커서 페이지네이션으로 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "댓글 목록 조회 성공"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 조회 조건"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "리뷰를 찾을 수 없음"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "서버 내부 오류"
            )
    })
    ResponseEntity<CommentListResponse> findAll(
            @ParameterObject
            @ModelAttribute CommentSearchRequest request
    );
}
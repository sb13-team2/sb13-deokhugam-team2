package com.deokhugam.review.controller.doc;

import com.deokhugam.review.dto.request.ReviewCreateRequest;
import com.deokhugam.review.dto.request.ReviewSearchRequest;
import com.deokhugam.review.dto.request.ReviewUpdateRequest;
import com.deokhugam.review.dto.response.ReviewDetailResponse;
import com.deokhugam.review.dto.response.ReviewLikeResponse;
import com.deokhugam.review.dto.response.ReviewListResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@Tag(name = "리뷰 관리", description = "리뷰 관련 API")
public interface ReviewControllerDoc {

    @Operation(
            operationId = "createReview",
            summary = "리뷰 등록",
            description = "새로운 리뷰를 등록합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "리뷰 등록 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (입력값 검증 실패)"),
            @ApiResponse(responseCode = "404", description = "도서 정보 없음"),
            @ApiResponse(responseCode = "409", description = "이미 작성된 리뷰 존재"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    ResponseEntity<ReviewDetailResponse> create(
            @Valid @RequestBody ReviewCreateRequest request
    );

    @Operation(
            operationId = "searchReviews",
            summary = "리뷰 목록 조회",
            description = "검색 조건에 맞는 리뷰 목록을 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "리뷰 목록 조회 성공"),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (정렬 기준 오류, 페이지네이션 파라미터 오류, 요청자 ID 누락)"
            ),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @Parameters({
            @Parameter(
                    name = "userId",
                    in = ParameterIn.QUERY,
                    description = "작성자 ID",
                    example = "123e4567-e89b-12d3-a456-426614174000",
                    schema = @Schema(
                            type = "string",
                            format = "uuid"
                    )
            ),
            @Parameter(
                    name = "bookId",
                    in = ParameterIn.QUERY,
                    description = "도서 ID",
                    example = "123e4567-e89b-12d3-a456-426614174000",
                    schema = @Schema(
                            type = "string",
                            format = "uuid"
                    )
            ),
            @Parameter(
                    name = "keyword",
                    in = ParameterIn.QUERY,
                    description = "검색 키워드 (작성자 닉네임 | 내용)",
                    example = "홍길동",
                    schema = @Schema(type = "string")
            ),
            @Parameter(
                    name = "orderBy",
                    in = ParameterIn.QUERY,
                    description = "정렬 기준(createdAt | rating)",
                    example = "createdAt",
                    schema = @Schema(
                            type = "string",
                            defaultValue = "createdAt"
                    )
            ),
            @Parameter(
                    name = "direction",
                    in = ParameterIn.QUERY,
                    description = "정렬 방향",
                    example = "DESC",
                    schema = @Schema(
                            type = "string",
                            defaultValue = "DESC",
                            allowableValues = {"ASC", "DESC"}
                    )
            ),
            @Parameter(
                    name = "cursor",
                    in = ParameterIn.QUERY,
                    description = "커서 페이지네이션 커서",
                    schema = @Schema(type = "string")
            ),
            @Parameter(
                    name = "after",
                    in = ParameterIn.QUERY,
                    description = "보조 커서(createdAt)",
                    schema = @Schema(
                            type = "string",
                            format = "date-time"
                    )
            ),
            @Parameter(
                    name = "limit",
                    in = ParameterIn.QUERY,
                    description = "페이지 크기",
                    example = "50",
                    schema = @Schema(
                            type = "integer",
                            format = "int32",
                            defaultValue = "50"
                    )
            )
    })
    ResponseEntity<ReviewListResponse> findAll(
            @Parameter(hidden = true)
            @Valid
            @ModelAttribute ReviewSearchRequest request,

            @Parameter(
                    description = "요청자 ID",
                    required = true,
                    example = "123e4567-e89b-12d3-a456-426614174000"
            )
            @RequestHeader("Deokhugam-Request-User-ID")
            UUID requesterId
    );

    @Operation(
            operationId = "getReview",
            summary = "리뷰 상세 정보 조회",
            description = "리뷰 ID로 상세 정보를 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "리뷰 정보 조회 성공"),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (요청자 ID 누락)"
            ),
            @ApiResponse(responseCode = "404", description = "리뷰 정보 없음"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    ResponseEntity<ReviewDetailResponse> findById(
            @Parameter(
                    description = "리뷰 ID",
                    required = true,
                    example = "123e4567-e89b-12d3-a456-426614174000"
            )
            @PathVariable UUID reviewId,

            @Parameter(
                    description = "요청자 ID",
                    required = true,
                    example = "123e4567-e89b-12d3-a456-426614174000"
            )
            @RequestHeader("Deokhugam-Request-User-ID")
            UUID requesterId
    );

    @Operation(
            operationId = "updateReview",
            summary = "리뷰 수정",
            description = "본인이 작성한 리뷰를 수정합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "리뷰 수정 성공"),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (입력값 검증 실패)"
            ),
            @ApiResponse(responseCode = "403", description = "리뷰 수정 권한 없음"),
            @ApiResponse(responseCode = "404", description = "리뷰 정보 없음"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    ResponseEntity<ReviewDetailResponse> update(
            @Parameter(
                    description = "리뷰 ID",
                    required = true,
                    example = "123e4567-e89b-12d3-a456-426614174000"
            )
            @PathVariable UUID reviewId,

            @Parameter(
                    description = "요청자 ID",
                    required = true,
                    example = "123e4567-e89b-12d3-a456-426614174000"
            )
            @RequestHeader("Deokhugam-Request-User-ID")
            UUID requesterId,

            @Valid
            @RequestBody ReviewUpdateRequest request
    );

    @Operation(
            operationId = "deleteReview",
            summary = "리뷰 논리 삭제",
            description = "본인이 작성한 리뷰를 논리적으로 삭제합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "리뷰 삭제 성공"),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (요청자 ID 누락)"
            ),
            @ApiResponse(responseCode = "403", description = "리뷰 삭제 권한 없음"),
            @ApiResponse(responseCode = "404", description = "리뷰 정보 없음"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    ResponseEntity<Void> softDelete(
            @Parameter(
                    description = "리뷰 ID",
                    required = true,
                    example = "123e4567-e89b-12d3-a456-426614174000"
            )
            @PathVariable UUID reviewId,

            @Parameter(
                    description = "요청자 ID",
                    required = true,
                    example = "123e4567-e89b-12d3-a456-426614174000"
            )
            @RequestHeader("Deokhugam-Request-User-ID")
            UUID requesterId
    );

    @Operation(
            operationId = "permanentDeleteReview",
            summary = "리뷰 물리 삭제",
            description = "본인이 작성한 리뷰를 물리적으로 삭제합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "리뷰 삭제 성공"),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (요청자 ID 누락)"
            ),
            @ApiResponse(responseCode = "403", description = "리뷰 삭제 권한 없음"),
            @ApiResponse(responseCode = "404", description = "리뷰 정보 없음"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    ResponseEntity<Void> hardDelete(
            @Parameter(
                    description = "리뷰 ID",
                    required = true,
                    example = "123e4567-e89b-12d3-a456-426614174000"
            )
            @PathVariable UUID reviewId,

            @Parameter(
                    description = "요청자 ID",
                    required = true,
                    example = "123e4567-e89b-12d3-a456-426614174000"
            )
            @RequestHeader("Deokhugam-Request-User-ID")
            UUID requesterId
    );

    @Operation(
            operationId = "likeReview",
            summary = "리뷰 좋아요",
            description = "리뷰에 좋아요를 추가하거나 취소합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "리뷰 좋아요 성공"),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (요청자 ID 누락)"
            ),
            @ApiResponse(responseCode = "404", description = "리뷰 정보 없음"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    ResponseEntity<ReviewLikeResponse> toggleLike(
            @Parameter(
                    description = "리뷰 ID",
                    required = true,
                    example = "123e4567-e89b-12d3-a456-426614174000"
            )
            @PathVariable UUID reviewId,

            @Parameter(
                    description = "요청자 ID",
                    required = true,
                    example = "123e4567-e89b-12d3-a456-426614174000"
            )
            @RequestHeader("Deokhugam-Request-User-ID")
            UUID requesterId
    );
}
package com.deokhugam.review.controller.doc;

import com.deokhugam.review.dto.request.ReviewCreateRequest;
import com.deokhugam.review.dto.request.ReviewSearchRequest;
import com.deokhugam.review.dto.request.ReviewUpdateRequest;
import com.deokhugam.review.dto.response.ReviewDetailResponse;
import com.deokhugam.review.dto.response.ReviewLikeResponse;
import com.deokhugam.review.dto.response.ReviewListResponse;
import io.swagger.v3.oas.annotations.Operation;
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
            summary = "리뷰 등록",
            description = "도서에 새로운 리뷰를 등록합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "리뷰 등록 성공"),
            @ApiResponse(responseCode = "400", description = "입력값 검증 실패"),
            @ApiResponse(responseCode = "404", description = "사용자 또는 도서 정보 없음"),
            @ApiResponse(responseCode = "409", description = "활성 리뷰 중복"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    ResponseEntity<ReviewDetailResponse> create(
            @Valid @RequestBody ReviewCreateRequest request
    );

    @Operation(
            summary = "리뷰 목록 조회",
            description = "검색 조건에 맞는 리뷰 목록을 커서 기반으로 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "리뷰 목록 조회 성공"),
            @ApiResponse(responseCode = "400", description = "검색 또는 커서 조건 오류"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    ResponseEntity<ReviewListResponse> findAll(
            @Valid @ModelAttribute ReviewSearchRequest request,
            @RequestHeader("Deokhugam-Request-User-ID") UUID requesterId
    );

    @Operation(
            summary = "리뷰 상세 정보 조회",
            description = "리뷰 ID로 상세 정보를 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "리뷰 상세 조회 성공"),
            @ApiResponse(responseCode = "404", description = "리뷰 정보 없음"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    ResponseEntity<ReviewDetailResponse> findById(
            @PathVariable UUID reviewId,
            @RequestHeader("Deokhugam-Request-User-ID") UUID requesterId
    );

    @Operation(
            summary = "리뷰 수정",
            description = "작성자가 자신의 리뷰 내용과 평점을 수정합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "리뷰 수정 성공"),
            @ApiResponse(responseCode = "400", description = "입력값 검증 실패"),
            @ApiResponse(responseCode = "403", description = "리뷰 수정 권한 없음"),
            @ApiResponse(responseCode = "404", description = "리뷰 정보 없음"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    ResponseEntity<ReviewDetailResponse> update(
            @PathVariable UUID reviewId,
            @RequestHeader("Deokhugam-Request-User-ID") UUID requesterId,
            @Valid @RequestBody ReviewUpdateRequest request
    );

    @Operation(
            summary = "리뷰 논리 삭제",
            description = "작성자가 자신의 리뷰를 논리적으로 삭제합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "리뷰 논리 삭제 성공"),
            @ApiResponse(responseCode = "403", description = "리뷰 삭제 권한 없음"),
            @ApiResponse(responseCode = "404", description = "리뷰 정보 없음"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    ResponseEntity<Void> softDelete(
            @PathVariable UUID reviewId,
            @RequestHeader("Deokhugam-Request-User-ID") UUID requesterId
    );

    @Operation(
            summary = "리뷰 물리 삭제",
            description = "관련 댓글·알림·좋아요·랭킹과 함께 리뷰를 물리적으로 삭제합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "리뷰 물리 삭제 성공"),
            @ApiResponse(responseCode = "403", description = "리뷰 삭제 권한 없음"),
            @ApiResponse(responseCode = "404", description = "리뷰 정보 없음"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    ResponseEntity<Void> hardDelete(
            @PathVariable UUID reviewId,
            @RequestHeader("Deokhugam-Request-User-ID") UUID requesterId
    );

    @Operation(
            summary = "리뷰 좋아요 등록 및 취소",
            description = "요청자의 리뷰 좋아요 상태를 등록하거나 취소합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "좋아요 상태 변경 성공"),
            @ApiResponse(responseCode = "404", description = "사용자 또는 리뷰 정보 없음"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    ResponseEntity<ReviewLikeResponse> toggleLike(
            @PathVariable UUID reviewId,
            @RequestHeader("Deokhugam-Request-User-ID") UUID requesterId
    );
}
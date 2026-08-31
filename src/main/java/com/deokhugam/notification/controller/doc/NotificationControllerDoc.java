package com.deokhugam.notification.controller.doc;

import com.deokhugam.notification.dto.request.NotificationUpdateRequest;
import com.deokhugam.notification.dto.response.NotificationDto;
import com.deokhugam.notification.dto.response.NotificationListResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "알림 관리", description = "알림 관련 API")
public interface NotificationControllerDoc {

  @Operation(
      operationId = "updateNotification",
      summary = "알림 읽음 상태 업데이트",
      description = "특정 알림의 읽음 상태를 업데이트합니다."
  )
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "알림 상태 업데이트 성공"),
      @ApiResponse(responseCode = "400", description = "잘못된 요청 (입력값 검증 실패, 요청자 ID 누락)"),
      @ApiResponse(responseCode = "403", description = "알림 수정 권한 없음"),
      @ApiResponse(responseCode = "404", description = "알림 정보 없음"),
      @ApiResponse(responseCode = "500", description = "서버 내부 오류")
  })
  ResponseEntity<NotificationDto> readNotification(
      @Parameter(description = "알림 ID", example = "123e4567-e89b-12d3-a456-426614174000")
      @PathVariable UUID notificationId,

      @Parameter(description = "요청자 ID", required = true,  example = "123e4567-e89b-12d3-a456-426614174000")
      @RequestHeader(value = "Deokhugam-Request-User-ID") UUID userId,

      @RequestBody NotificationUpdateRequest request
  );

  @Operation(
      operationId = "markAllAsRead",
      summary = "모든 알림 읽음 처리",
      description = "사용자의 모든 알림의 읽음 상태로 처리합니다."
  )
  @ApiResponses({
      @ApiResponse(responseCode = "204", description = "알림 읽음 처리 성공"),
      @ApiResponse(responseCode = "400", description = "잘못된 요청 (사용자 ID 누락)"),
      @ApiResponse(responseCode = "404", description = "사용자 정보 없음"),
      @ApiResponse(responseCode = "500", description = "서버 내부 오류")
  })
  ResponseEntity<Void> readAllNotification(
      @Parameter(description = "사용자 ID", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
      @RequestHeader(value = "Deokhugam-Request-User-ID") UUID userId
  );

  @Operation(
      operationId = "getNotifications",
      summary = "알림 목록 조회",
      description = "사용자의 알림 목록을 조회합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "알림 목록 조회 성공"),
      @ApiResponse(responseCode = "400", description = "잘못된 요청 (정렬 방향 오류, 페이지네이션 파라미터 오류, 사용자 ID 누락)"),
      @ApiResponse(responseCode = "404", description = "사용자 정보 없음"),
      @ApiResponse(responseCode = "500", description = "서버 내부 오류")
  })
  ResponseEntity<NotificationListResponse> getNotifications(
      @Parameter(description = "사용자 ID", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
      @RequestParam UUID userId,

      @Parameter(description = "정렬 방향", example = "DESC", schema = @Schema(type = "string", allowableValues = {"ASC", "DESC"}, defaultValue = "DESC"))
      @Pattern(regexp = "^(ASC|DESC)$", message = "정렬 방향은 ASC 또는 DESC만 허용됩니다.")
      @RequestParam(required = false, defaultValue = "DESC") String direction,

      @Parameter(description = "커서 페이지네이션 커서")
      @RequestParam(required = false) String cursor,

      @Parameter(description = "보조 커서(createdAt)")
      @RequestParam(required = false) LocalDateTime after,

      @Parameter(description = "페이지 크기", example = "20")
      @RequestParam(required = false, defaultValue = "20") int limit
  );
}

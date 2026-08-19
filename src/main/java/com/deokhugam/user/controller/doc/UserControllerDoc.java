package com.deokhugam.user.controller.doc;

import com.deokhugam.user.entity.Period;
import com.deokhugam.user.dto.response.CursorPageResponsePowerUserDto;
import com.deokhugam.user.dto.request.UserRegisterRequest;
import com.deokhugam.user.dto.request.UserLoginRequest;
import com.deokhugam.user.dto.request.UserUpdateRequest;
import com.deokhugam.user.dto.response.UserDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Tag(name = "사용자 관리", description = "사용자 관련 API")
public interface UserControllerDoc {

    @Operation(summary = "회원가입", description = "새로운 사용자를 등록합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "회원가입 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (입력값 검증 실패)"),
            @ApiResponse(responseCode = "409", description = "이메일 중복"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    ResponseEntity<UserDto> register(@RequestBody UserRegisterRequest request);

    @Operation(summary = "로그인", description = "사용자 로그인을 처리합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그인 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "401", description = "로그인 실패 (이메일 또는 비밀번호 불일치)"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    ResponseEntity<UserDto> login(@RequestBody UserLoginRequest request);

    @Operation(summary = "사용자 정보 조회", description = "사용자 ID로 상세 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "사용자 정보 조회 성공"),
            @ApiResponse(responseCode = "404", description = "사용자 정보 없음")
    })
    ResponseEntity<UserDto> getUser(@PathVariable UUID userId);

    @Operation(summary = "사용자 정보 수정", description = "사용자의 닉네임을 수정합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "사용자 정보 수정 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (입력값 검증 실패)"),
            @ApiResponse(responseCode = "403", description = "사용자 정보 수정 권한 없음"),
            @ApiResponse(responseCode = "404", description = "사용자 정보 없음")
    })
    ResponseEntity<UserDto> updateUser(@PathVariable UUID userId, @RequestBody UserUpdateRequest request);

    @Operation(summary = "사용자 논리 삭제", description = "사용자를 논리적으로 삭제합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "사용자 삭제 성공"),
            @ApiResponse(responseCode = "403", description = "사용자 삭제 권한 없음"),
            @ApiResponse(responseCode = "404", description = "사용자 정보 없음"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    ResponseEntity<Void> softDeleteUser(@PathVariable UUID userId);

    @Operation(summary = "사용자 물리 삭제", description = "사용자를 물리적으로 삭제합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "사용자 삭제 성공"),
            @ApiResponse(responseCode = "403", description = "사용자 삭제 권한 없음"),
            @ApiResponse(responseCode = "404", description = "사용자 정보 없음"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    ResponseEntity<Void> hardDeleteUser(@PathVariable UUID userId);

    @Operation(summary = "파워 유저 목록 조회", description = "기간별 파워 유저 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "파워 유저 목록 조회 성공")
    })
    ResponseEntity<CursorPageResponsePowerUserDto> getPowerUsers(
            @RequestParam(defaultValue = "DAILY") Period period,
            @RequestParam(defaultValue = "ASC") String direction,
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime after,
            @RequestParam(defaultValue = "50") int limit
    );
}
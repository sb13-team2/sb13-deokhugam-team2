package com.deokhugam.user.controller;

import com.deokhugam.user.controller.doc.UserControllerDoc;
import com.deokhugam.user.entity.Period;
import com.deokhugam.user.dto.response.CursorPageResponsePowerUserDto;
import com.deokhugam.user.dto.request.UserRegisterRequest;
import com.deokhugam.user.dto.request.UserLoginRequest;
import com.deokhugam.user.dto.request.UserUpdateRequest;
import com.deokhugam.user.dto.response.UserDto;
import com.deokhugam.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController implements UserControllerDoc {

    private final UserService userService;

    @Override
    @PostMapping
    public ResponseEntity<UserDto> register(@Valid @RequestBody UserRegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.register(request));
    }

    @Override
    @PostMapping("/login")
    public ResponseEntity<UserDto> login(@Valid @RequestBody UserLoginRequest request) {
        UserDto response = userService.login(request);
        // 로그인 성공 시 Deokhugam-Request-User-ID 헤더에 UUID 포함
        return ResponseEntity.ok()
                .header("Deokhugam-Request-User-ID", response.id().toString())
                .body(response);
    }

    @Override
    @GetMapping("/{userId}")
    public ResponseEntity<UserDto> getUser(@PathVariable UUID userId) {
        return ResponseEntity.ok(userService.getUser(userId));
    }

    @Override
    @PatchMapping("/{userId}")
    public ResponseEntity<UserDto> updateUser(@PathVariable UUID userId, @Valid @RequestBody UserUpdateRequest request) {
        return ResponseEntity.ok(userService.update(userId, request));
    }

    @Override
    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> softDeleteUser(@PathVariable UUID userId) {
        userService.softDelete(userId);
        return ResponseEntity.noContent().build();
    }

    @Override
    @DeleteMapping("/{userId}/hard")
    public ResponseEntity<Void> hardDeleteUser(@PathVariable UUID userId) {
        userService.hardDelete(userId);
        return ResponseEntity.noContent().build();
    }

    @Override
    @GetMapping("/power")
    public ResponseEntity<CursorPageResponsePowerUserDto> getPowerUsers(
            @RequestParam(defaultValue = "DAILY") Period period,
            @RequestParam(defaultValue = "ASC") String direction,
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime after,
            @RequestParam(defaultValue = "50") int limit) {
        return ResponseEntity.ok(userService.getPowerUsers(period, direction, cursor, after, limit));
    }
}
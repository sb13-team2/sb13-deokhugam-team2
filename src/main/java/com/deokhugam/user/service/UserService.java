package com.deokhugam.user.service;

import com.deokhugam.global.exception.ErrorCode;
import com.deokhugam.user.entity.Period;
import com.deokhugam.user.entity.User;
import com.deokhugam.user.dto.response.CursorPageResponsePowerUserDto;
import com.deokhugam.user.dto.request.UserRegisterRequest;
import com.deokhugam.user.dto.request.UserLoginRequest;
import com.deokhugam.user.dto.request.UserUpdateRequest;
import com.deokhugam.user.dto.response.PowerUserDto;
import com.deokhugam.user.dto.response.UserDto;
import com.deokhugam.user.exception.*;
import com.deokhugam.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Slice;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserDto register(UserRegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new UserException(ErrorCode.EMAIL_DUPLICATION, Map.of("email", request.email()));
        }

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(request.password());

        User user = User.create(request.email(), request.nickname(), encodedPassword);
        User savedUser = userRepository.save(user);
        return UserDto.from(savedUser);
    }

    public UserDto login(UserLoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .filter(u -> u.getDeletedAt() == null)
                .orElseThrow(() -> new UserException(ErrorCode.LOGIN_INPUT_INVALID));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new UserException(ErrorCode.LOGIN_INPUT_INVALID);
        }

        return UserDto.from(user);
    }

    public UserDto getUser(UUID userId) {
        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND, Map.of("userId", userId)));
        return UserDto.from(user);
    }

    @Transactional
    public UserDto update(UUID userId, UserUpdateRequest request) {
        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND, Map.of("userId", userId)));

        user.updateNickname(request.nickname());
        return UserDto.from(user);
    }

    @Transactional
    public void softDelete(UUID userId) {
        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND, Map.of("userId", userId)));
        user.softDelete();
    }

    @Transactional
    public void hardDelete(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new UserException(ErrorCode.USER_NOT_FOUND, Map.of("userId", userId));
        }
        userRepository.deleteById(userId);
    }

    public CursorPageResponsePowerUserDto getPowerUsers(Period period, String direction, String cursor, LocalDateTime after, int limit) {
        Slice<PowerUserDto> slice = userRepository.findPowerUsers(period, direction, cursor, after, limit);

        List<PowerUserDto> content = slice.getContent();
        String nextCursor = null;
        LocalDateTime nextAfter = null;

        if (!content.isEmpty()) {
            PowerUserDto last = content.get(content.size() - 1);
            nextCursor = last.userId().toString();
            nextAfter = last.createdAt();
        }

        return new CursorPageResponsePowerUserDto(
                content,
                nextCursor,
                nextAfter,
                content.size(),
                userRepository.count(),
                slice.hasNext()
        );
    }

    /**
     * 프로토타입 요구사항: 원활한 테스트 환경을 위해 논리 삭제 후 5분 뒤 복구
     */
    @Scheduled(fixedRateString = "60000")
    @Transactional
    public void restoreSoftDeletedUsers() {
        LocalDateTime fiveMinsAgo = LocalDateTime.now().minusMinutes(5);
        List<User> usersToRestore = userRepository.findSoftDeletedBefore(fiveMinsAgo);

        for (User user : usersToRestore) {
            user.restore();
            log.info("Prototype Test: User restored after 5 mins of soft delete. ID: {}", user.getId());
        }
    }
}
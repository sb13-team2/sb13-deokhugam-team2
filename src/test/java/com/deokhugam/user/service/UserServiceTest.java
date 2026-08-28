package com.deokhugam.user.service;

import com.deokhugam.comment.repository.CommentRepository;
import com.deokhugam.dashboard.repository.ReviewRankingRepository;
import com.deokhugam.dashboard.repository.UserRankingRepository;
import com.deokhugam.global.exception.ErrorCode;
import com.deokhugam.notification.repository.NotificationRepository;
import com.deokhugam.review.entity.Review;
import com.deokhugam.review.repository.ReviewLikeRepository;
import com.deokhugam.review.repository.ReviewRepository;
import com.deokhugam.user.dto.request.UserLoginRequest;
import com.deokhugam.user.dto.request.UserRegisterRequest;
import com.deokhugam.user.dto.request.UserUpdateRequest;
import com.deokhugam.user.dto.response.UserDto;
import com.deokhugam.user.entity.User;
import com.deokhugam.user.exception.UserException;
import com.deokhugam.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;

    @Mock private ReviewRepository reviewRepository;
    @Mock private ReviewLikeRepository reviewLikeRepository;
    @Mock private CommentRepository commentRepository;
    @Mock private NotificationRepository notificationRepository;
    @Mock private UserRankingRepository userRankingRepository;
    @Mock private ReviewRankingRepository reviewRankingRepository;

    @Test
    @DisplayName("회원가입 - 성공")
    void register_success() {
        // given
        UserRegisterRequest request = new UserRegisterRequest("test@test.com", "nickname", "password123!");
        given(userRepository.existsByEmail(request.email())).willReturn(false);
        given(passwordEncoder.encode(request.password())).willReturn("encodedPassword");

        User user = User.create(request.email(), request.nickname(), "encodedPassword");
        given(userRepository.save(any(User.class))).willReturn(user);

        // when
        UserDto response = userService.register(request);

        // then
        assertNotNull(response);
        assertEquals("test@test.com", response.email());
    }

    @Test
    @DisplayName("회원가입 - 실패 (이메일 중복)")
    void register_fail_duplicateEmail() {
        // given
        UserRegisterRequest request = new UserRegisterRequest("test@test.com", "nickname", "password123!");
        given(userRepository.existsByEmail(request.email())).willReturn(true);

        // when & then
        UserException exception = assertThrows(UserException.class, () -> userService.register(request));
        assertEquals(ErrorCode.EMAIL_DUPLICATION, exception.getErrorCode());
    }

    @Test
    @DisplayName("로그인 - 성공")
    void login_success() {
        // given
        UserLoginRequest request = new UserLoginRequest("test@test.com", "password123!");
        User user = User.create("test@test.com", "nickname", "encodedPassword");

        given(userRepository.findByEmail(request.email())).willReturn(Optional.of(user));
        given(passwordEncoder.matches(request.password(), user.getPassword())).willReturn(true);

        // when
        UserDto response = userService.login(request);

        // then
        assertNotNull(response);
        assertEquals("test@test.com", response.email());
    }

    @Test
    @DisplayName("사용자 조회 - 성공")
    void getUser_success() {
        // given
        UUID userId = UUID.randomUUID();
        User user = User.create("test@test.com", "nickname", "encodedPassword");
        given(userRepository.findByIdAndDeletedAtIsNull(userId)).willReturn(Optional.of(user));

        // when
        UserDto response = userService.getUser(userId);

        // then
        assertNotNull(response);
        assertEquals("nickname", response.nickname());
    }

    @Test
    @DisplayName("사용자 수정 - 성공")
    void update_success() {
        // given
        UUID userId = UUID.randomUUID();
        UserUpdateRequest request = new UserUpdateRequest("newNickname");
        User user = User.create("test@test.com", "oldNickname", "password");
        given(userRepository.findByIdAndDeletedAtIsNull(userId)).willReturn(Optional.of(user));

        // when
        UserDto response = userService.update(userId, request);

        // then
        assertEquals("newNickname", response.nickname());
        assertEquals("newNickname", user.getNickname());
    }

    @Test
    @DisplayName("소프트 삭제 - 성공")
    void softDelete_success() {
        // given
        UUID userId = UUID.randomUUID();
        User user = User.create("test@test.com", "nickname", "password");
        given(userRepository.findByIdAndDeletedAtIsNull(userId)).willReturn(Optional.of(user));

        // when
        userService.softDelete(userId);

        // then
        assertNotNull(user.getDeletedAt());
    }

    @Test
    @DisplayName("하드 삭제 - 성공 (연관된 모든 데이터 삭제 검증)")
    void hardDelete_success() {
        // given
        UUID userId = UUID.randomUUID();
        given(userRepository.existsById(userId)).willReturn(true);

        // 작성한 리뷰 Mock 객체 생성
        Review mockReview = mock(Review.class);
        UUID reviewId = UUID.randomUUID();
        given(mockReview.getId()).willReturn(reviewId);
        List<Review> userReviews = List.of(mockReview);

        given(reviewRepository.findAllByUserId(userId)).willReturn(userReviews);

        // when
        userService.hardDelete(userId);

        // then
        // 1. 유저가 작성한 리뷰 연관 데이터 삭제 확인
        verify(reviewLikeRepository, times(1)).deleteAllByReviewId(reviewId);
        verify(commentRepository, times(1)).deleteAllByReviewId(reviewId);
        verify(notificationRepository, times(1)).deleteAllByReviewId(reviewId);
        verify(reviewRankingRepository, times(1)).deleteAllByReviewId(reviewId);
        verify(reviewRepository, times(1)).deleteAll(userReviews);

        // 2. 유저가 활동한 데이터 삭제 확인
        verify(reviewLikeRepository, times(1)).deleteAllByUserId(userId);
        verify(commentRepository, times(1)).deleteAllByUserId(userId);
        verify(notificationRepository, times(1)).deleteAllByUserId(userId);
        verify(userRankingRepository, times(1)).deleteAllByUserId(userId);

        // 3. 최종 유저 삭제 확인
        verify(userRepository, times(1)).deleteById(userId);
    }

    @Test
    @DisplayName("오래된 소프트 삭제 유저 하드 삭제 처리 스케줄러 - 성공")
    void processHardDeleteForOldSoftDeletedUsers_success() {
        // given
        UUID userId = UUID.randomUUID();
        User mockUser = mock(User.class);
        given(mockUser.getId()).willReturn(userId);

        // 하루 전 삭제된 유저 리스트 반환 설정
        given(userRepository.findSoftDeletedBefore(any(LocalDateTime.class))).willReturn(List.of(mockUser));
        // 내부 로직에서 hardDelete()를 호출하므로, 존재하는 유저라고 가정
        given(userRepository.existsById(userId)).willReturn(true);

        // when
        userService.processHardDeleteForOldSoftDeletedUsers();

        // then
        // hardDelete가 호출되어 최종적으로 deleteById가 실행되었는지 확인
        verify(userRepository, times(1)).deleteById(userId);
    }
}
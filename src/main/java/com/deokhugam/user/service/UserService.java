package com.deokhugam.user.service;

import com.deokhugam.comment.entity.Comment;
import com.deokhugam.comment.repository.CommentRepository;
import com.deokhugam.dashboard.repository.ReviewRankingRepository;
import com.deokhugam.dashboard.repository.UserRankingRepository;
import com.deokhugam.global.exception.ErrorCode;
import com.deokhugam.notification.repository.NotificationRepository;
import com.deokhugam.review.entity.Review;
import com.deokhugam.review.entity.ReviewLike;
import com.deokhugam.review.repository.ReviewLikeRepository;
import com.deokhugam.review.repository.ReviewRepository;
import com.deokhugam.user.entity.User;
import com.deokhugam.user.dto.request.UserRegisterRequest;
import com.deokhugam.user.dto.request.UserLoginRequest;
import com.deokhugam.user.dto.request.UserUpdateRequest;
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

    private final ReviewRepository reviewRepository;
    private final ReviewLikeRepository reviewLikeRepository;
    private final CommentRepository commentRepository;
    private final NotificationRepository notificationRepository;
    private final UserRankingRepository userRankingRepository;
    private final ReviewRankingRepository reviewRankingRepository;

    @Transactional
    public UserDto register(UserRegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new UserException(ErrorCode.EMAIL_DUPLICATION, Map.of("email", request.email()));
        }

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

        // 1. 유저가 작성한 리뷰 조회 및 해당 리뷰에 종속된 모든 연관 데이터 삭제
        List<Review> userReviews = reviewRepository.findAllByUserId(userId);
        for (Review review : userReviews) {
            UUID reviewId = review.getId();
            reviewLikeRepository.deleteAllByReviewId(reviewId);
            commentRepository.deleteAllByReviewId(reviewId);
            notificationRepository.deleteAllByReviewId(reviewId);
            reviewRankingRepository.deleteAllByReviewId(reviewId);
        }
        // 리뷰 물리 삭제
        reviewRepository.deleteAll(userReviews);

        // 2. 다른 사람의 리뷰에 남긴 좋아요와 댓글 수치 차감
        List<ReviewLike> userLikes = reviewLikeRepository.findAllByUserId(userId);
        for (ReviewLike like : userLikes) {
            like.getReview().decreaseLikeCount();
        }

        // 댓글 수 차감
        List<Comment> userComments = commentRepository.findAllByUserId(userId);
        for (Comment comment : userComments) {
            reviewRepository.findByIdAndDeletedAtIsNull(comment.getReviewId())
                    .ifPresent(Review::decreaseCommentCount);
        }

        // 3. 유저 본인이 남긴 타인의 리뷰에 대한 좋아요, 댓글, 알림, 랭킹 정보 삭제
        reviewLikeRepository.deleteAllByUserId(userId);
        commentRepository.deleteAllByUserId(userId);
        notificationRepository.deleteAllByUserId(userId);
        userRankingRepository.deleteAllByUserId(userId);

        // 4. 최종적으로 유저 물리 삭제
        userRepository.deleteById(userId);
    }

    /**
     * 논리 삭제 후 1일(24시간) 경과된 유저 정보를 완전히 삭제하는 스케줄러
     * 매일 새벽 3시에 실행되도록 설정 (cron 변경 가능)
     */
    @Scheduled(cron = "0 0 3 * * *", zone = "Asia/Seoul")
    @Transactional
    public void processHardDeleteForOldSoftDeletedUsers() {
        // 현재 시간 기준으로 1일 전 시간 계산
        LocalDateTime oneDayAgo = LocalDateTime.now().minusDays(1);

        // 1일 이전에 논리 삭제된 유저 목록 조회
        List<User> usersToDelete = userRepository.findSoftDeletedBefore(oneDayAgo);

        for (User user : usersToDelete) {
            hardDelete(user.getId());
            log.info("유저 물리삭제는 논리삭제 후 1일이 지난 후 적용됩니다.. ID: {}", user.getId());
        }
    }
}
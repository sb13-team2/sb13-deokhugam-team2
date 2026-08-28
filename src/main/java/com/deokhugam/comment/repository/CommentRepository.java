package com.deokhugam.comment.repository;

import com.deokhugam.comment.entity.Comment;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository
        extends JpaRepository<Comment, UUID>,
        CommentRepositoryCustom {

    void deleteAllByReviewId(UUID reviewId);

    // 유저가 남긴 댓글 전체 삭제를 위해 추가
    void deleteAllByUserId(UUID userId);
}
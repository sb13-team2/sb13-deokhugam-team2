package com.deokhugam.comment.repository;

import com.deokhugam.comment.entity.Comment;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository
        extends JpaRepository<Comment, UUID>,
        CommentRepositoryCustom {
}
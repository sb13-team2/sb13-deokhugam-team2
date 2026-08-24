package com.deokhugam.comment.repository;

import com.deokhugam.comment.dto.request.CommentSearchRequest;
import com.deokhugam.comment.entity.Comment;
import java.util.List;

public interface CommentRepositoryCustom {

    List<Comment> findAllByCursor(
            CommentSearchRequest request
    );

    long countAll(
            CommentSearchRequest request
    );
}
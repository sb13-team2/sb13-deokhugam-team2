package com.deokhugam.comment.service;

import com.deokhugam.comment.dto.request.CommentCreateRequest;
import com.deokhugam.comment.dto.request.CommentSearchRequest;
import com.deokhugam.comment.dto.request.CommentUpdateRequest;
import com.deokhugam.comment.dto.response.CommentListResponse;
import com.deokhugam.comment.dto.response.CommentResponse;
import java.util.UUID;

public interface CommentService {

    CommentResponse create(
            CommentCreateRequest request
    );

    CommentResponse update(
            UUID commentId,
            UUID requesterId,
            CommentUpdateRequest request
    );

    void delete(
            UUID commentId,
            UUID requesterId
    );

    void deleteAllByUserId(
            UUID userId
    );

    CommentListResponse findAll(
            CommentSearchRequest request
    );
}
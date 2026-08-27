package com.deokhugam.comment.controller;

import com.deokhugam.comment.controller.doc.CommentControllerDoc;
import com.deokhugam.comment.dto.request.CommentCreateRequest;
import com.deokhugam.comment.dto.request.CommentSearchRequest;
import com.deokhugam.comment.dto.request.CommentUpdateRequest;
import com.deokhugam.comment.dto.response.CommentListResponse;
import com.deokhugam.comment.dto.response.CommentResponse;
import com.deokhugam.comment.service.CommentService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController implements CommentControllerDoc {

    private static final String REQUEST_USER_ID_HEADER =
            "Deokhugam-Request-User-ID";

    private final CommentService commentService;

    @Override
    @PostMapping
    public ResponseEntity<CommentResponse> create(
            @Valid @RequestBody CommentCreateRequest request
    ) {
        CommentResponse response =
                commentService.create(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @Override
    @PatchMapping("/{commentId}")
    public ResponseEntity<CommentResponse> update(
            @PathVariable UUID commentId,
            @RequestHeader(REQUEST_USER_ID_HEADER) UUID requesterId,
            @Valid @RequestBody CommentUpdateRequest request
    ) {
        CommentResponse response =
                commentService.update(
                        commentId,
                        requesterId,
                        request
                );

        return ResponseEntity.ok(response);
    }

    @Override
    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID commentId,
            @RequestHeader(REQUEST_USER_ID_HEADER) UUID requesterId
    ) {
        commentService.delete(
                commentId,
                requesterId
        );

        return ResponseEntity
                .noContent()
                .build();
    }

    @Override
    @GetMapping
    public ResponseEntity<CommentListResponse> findAll(
            @Valid @ModelAttribute CommentSearchRequest request
    ) {
        CommentListResponse response =
                commentService.findAll(request);

        return ResponseEntity.ok(response);
    }
}
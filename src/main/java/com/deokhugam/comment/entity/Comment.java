package com.deokhugam.comment.entity;

import com.deokhugam.global.entity.SoftDeleteEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "comments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment extends SoftDeleteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 1000)
    private String content;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "review_id", nullable = false)
    private UUID reviewId;

    public Comment(
            String content,
            UUID userId,
            UUID reviewId
    ) {
        this.content = content;
        this.userId = userId;
        this.reviewId = reviewId;
    }

    public void updateContent(String content) {
        this.content = content;
    }
}
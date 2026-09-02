package com.deokhugam.review.dto.request;

import static org.assertj.core.api.Assertions.assertThat;

import com.deokhugam.review.dto.ReviewCursor;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ReviewSearchRequestTest {

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        validatorFactory.close();
    }

    @Test
    void 목록_조회_기본값을_설정한다() {
        ReviewSearchRequest request = new ReviewSearchRequest(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );

        assertThat(request.orderBy()).isEqualTo("createdAt");
        assertThat(request.direction()).isEqualTo("DESC");
        assertThat(request.limit()).isEqualTo(50);
    }

    @Test
    void 유효한_목록_조회_조건은_검증을_통과한다() {
        ReviewSearchRequest request = new ReviewSearchRequest(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "좋은 책",
                "rating",
                "ASC",
                ReviewCursor.encode(4, UUID.randomUUID()),
                LocalDateTime.of(2026, 8, 21, 10, 0),
                20
        );

        Set<ConstraintViolation<ReviewSearchRequest>> violations =
                validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    void 정렬_기준이_올바르지_않으면_검증에_실패한다() {
        ReviewSearchRequest request = new ReviewSearchRequest(
                null,
                null,
                null,
                "likeCount",
                "DESC",
                null,
                null,
                20
        );

        Set<ConstraintViolation<ReviewSearchRequest>> violations =
                validator.validate(request);

        assertThat(violations)
                .extracting(violation ->
                        violation.getPropertyPath().toString()
                )
                .contains("orderBy");
    }

    @Test
    void 정렬_방향이_올바르지_않으면_검증에_실패한다() {
        ReviewSearchRequest request = new ReviewSearchRequest(
                null,
                null,
                null,
                "createdAt",
                "SIDE",
                null,
                null,
                20
        );

        Set<ConstraintViolation<ReviewSearchRequest>> violations =
                validator.validate(request);

        assertThat(violations)
                .extracting(violation ->
                        violation.getPropertyPath().toString()
                )
                .contains("direction");
    }

    @Test
    void 페이지_크기가_1보다_작으면_검증에_실패한다() {
        ReviewSearchRequest request = new ReviewSearchRequest(
                null,
                null,
                null,
                "createdAt",
                "DESC",
                null,
                null,
                0
        );

        Set<ConstraintViolation<ReviewSearchRequest>> violations =
                validator.validate(request);

        assertThat(violations)
                .extracting(violation ->
                        violation.getPropertyPath().toString()
                )
                .contains("limit");
    }

    @Test
    void 소문자_정렬_방향은_검증에_실패한다() {
        ReviewSearchRequest request = new ReviewSearchRequest(
                null,
                null,
                null,
                "createdAt",
                "desc",
                null,
                null,
                20
        );

        Set<ConstraintViolation<ReviewSearchRequest>> violations =
                validator.validate(request);

        assertThat(violations)
                .extracting(violation ->
                        violation.getPropertyPath().toString()
                )
                .contains("direction");
    }

    @Test
    void 평점_정렬의_커서가_숫자가_아니면_검증에_실패한다() {
        ReviewSearchRequest request = new ReviewSearchRequest(
                null,
                null,
                null,
                "rating",
                "DESC",
                "not-a-number",
                null,
                20
        );

        Set<ConstraintViolation<ReviewSearchRequest>> violations =
                validator.validate(request);

        assertThat(violations)
                .extracting(ConstraintViolation::getMessage)
                .contains("커서 형식이 올바르지 않습니다.");
    }

    @Test
    void 생성일_정렬의_커서가_날짜가_아니면_검증에_실패한다() {
        ReviewSearchRequest request = new ReviewSearchRequest(
                null,
                null,
                null,
                "createdAt",
                "DESC",
                "not-a-date",
                null,
                20
        );

        Set<ConstraintViolation<ReviewSearchRequest>> violations =
                validator.validate(request);

        assertThat(violations)
                .extracting(ConstraintViolation::getMessage)
                .contains("커서 형식이 올바르지 않습니다.");
    }

    @Test
    void 평점_정렬에서_커서만_있고_after가_없으면_검증에_실패한다() {
        ReviewSearchRequest request = new ReviewSearchRequest(
                null,
                null,
                null,
                "rating",
                "DESC",
                ReviewCursor.encode(4, UUID.randomUUID()),
                null,
                20
        );

        Set<ConstraintViolation<ReviewSearchRequest>> violations =
                validator.validate(request);

        assertThat(violations)
                .extracting(ConstraintViolation::getMessage)
                .contains(
                        "평점 정렬에서 cursor를 사용하는 경우 after가 필요합니다."
                );
    }
}
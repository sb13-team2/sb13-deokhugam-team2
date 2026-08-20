package com.deokhugam.review.dto.request;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ReviewUpdateRequestTest {

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
    void 유효한_리뷰_수정_요청은_검증을_통과한다() {
        ReviewUpdateRequest request = new ReviewUpdateRequest(
                "수정한 리뷰 내용입니다.",
                4
        );

        Set<ConstraintViolation<ReviewUpdateRequest>> violations =
                validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    void 리뷰_내용이_공백이면_검증에_실패한다() {
        ReviewUpdateRequest request = new ReviewUpdateRequest(
                " ",
                4
        );

        Set<ConstraintViolation<ReviewUpdateRequest>> violations =
                validator.validate(request);

        assertThat(violations)
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("content");
    }

    @Test
    void 평점이_null이면_검증에_실패한다() {
        ReviewUpdateRequest request = new ReviewUpdateRequest(
                "수정한 리뷰 내용입니다.",
                null
        );

        Set<ConstraintViolation<ReviewUpdateRequest>> violations =
                validator.validate(request);

        assertThat(violations)
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("rating");
    }

    @Test
    void 평점이_1보다_작으면_검증에_실패한다() {
        ReviewUpdateRequest request = new ReviewUpdateRequest(
                "수정한 리뷰 내용입니다.",
                0
        );

        Set<ConstraintViolation<ReviewUpdateRequest>> violations =
                validator.validate(request);

        assertThat(violations)
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("rating");
    }

    @Test
    void 평점이_5보다_크면_검증에_실패한다() {
        ReviewUpdateRequest request = new ReviewUpdateRequest(
                "수정한 리뷰 내용입니다.",
                6
        );

        Set<ConstraintViolation<ReviewUpdateRequest>> violations =
                validator.validate(request);

        assertThat(violations)
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("rating");
    }
}
package com.deokhugam.review.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.deokhugam.book.entity.Book;
import com.deokhugam.book.repository.BookRepository;
import com.deokhugam.global.config.JpaConfig;
import com.deokhugam.review.dto.ReviewCursor;
import com.deokhugam.review.dto.request.ReviewSearchRequest;
import com.deokhugam.review.entity.Review;
import com.deokhugam.user.entity.User;
import com.deokhugam.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import(JpaConfig.class)
class ReviewRepositoryCustomTest {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void 키워드가_작성자_닉네임_내용_도서_제목에_부분_일치하는_리뷰를_조회한다() {
        User nicknameUser = saveUser("검색어독서가");
        User contentUser = saveUser("내용작성자");
        User bookUser = saveUser("도서작성자");

        Book normalBook1 = saveBook("일반 도서 1");
        Book normalBook2 = saveBook("일반 도서 2");
        Book keywordBook = saveBook("검색어가 포함된 도서");

        Review nicknameReview = saveReview(
                nicknameUser,
                normalBook1,
                "평범한 내용",
                5
        );
        Review contentReview = saveReview(
                contentUser,
                normalBook2,
                "리뷰 내용에 검색어가 있습니다.",
                4
        );
        Review bookReview = saveReview(
                bookUser,
                keywordBook,
                "다른 내용",
                3
        );

        ReviewSearchRequest request = new ReviewSearchRequest(
                null,
                null,
                "검색어",
                "createdAt",
                "DESC",
                null,
                null,
                50
        );

        List<Review> result =
                reviewRepository.findAllByCursor(request);

        assertThat(result)
                .extracting(Review::getId)
                .containsExactlyInAnyOrder(
                        nicknameReview.getId(),
                        contentReview.getId(),
                        bookReview.getId()
                );
    }

    @Test
    void 작성자와_도서_조건이_모두_일치하는_리뷰만_조회한다() {
        User targetUser = saveUser("대상 작성자");
        User otherUser = saveUser("다른 작성자");

        Book targetBook = saveBook("대상 도서");
        Book otherBook = saveBook("다른 도서");

        Review targetReview = saveReview(
                targetUser,
                targetBook,
                "대상 리뷰",
                5
        );
        saveReview(
                targetUser,
                otherBook,
                "다른 도서 리뷰",
                4
        );
        saveReview(
                otherUser,
                targetBook,
                "다른 작성자 리뷰",
                3
        );

        ReviewSearchRequest request = new ReviewSearchRequest(
                targetUser.getId(),
                targetBook.getId(),
                null,
                "createdAt",
                "DESC",
                null,
                null,
                50
        );

        List<Review> result =
                reviewRepository.findAllByCursor(request);

        assertThat(result)
                .extracting(Review::getId)
                .containsExactly(targetReview.getId());
    }

    @Test
    void 평점_내림차순_커서_다음의_리뷰를_조회한다() {
        User user1 = saveUser("작성자 1");
        User user2 = saveUser("작성자 2");
        User user3 = saveUser("작성자 3");

        Book book = saveBook("커서 테스트 도서");

        Review ratingFiveReview =
                saveReview(user1, book, "5점 리뷰", 5);

        Review cursorReview =
                saveReview(user2, book, "4점 리뷰", 4);

        Review ratingThreeReview =
                saveReview(user3, book, "3점 리뷰", 3);

        LocalDateTime fixedCreatedAt =
                LocalDateTime.of(2026, 8, 21, 10, 0);

        updateCreatedAt(
                ratingFiveReview.getId(),
                fixedCreatedAt
        );
        updateCreatedAt(
                cursorReview.getId(),
                fixedCreatedAt
        );
        updateCreatedAt(
                ratingThreeReview.getId(),
                fixedCreatedAt
        );

        entityManager.clear();

        ReviewSearchRequest request = new ReviewSearchRequest(
                null,
                book.getId(),
                null,
                "rating",
                "DESC",
                ReviewCursor.encode(
                        cursorReview.getRating(),
                        cursorReview.getId()
                ),
                fixedCreatedAt,
                50
        );

        List<Review> result =
                reviewRepository.findAllByCursor(request);

        assertThat(result)
                .extracting(Review::getId)
                .containsExactly(ratingThreeReview.getId());
    }

    @Test
    void 논리_삭제된_리뷰는_목록과_전체_개수에서_제외한다() {
        User activeUser = saveUser("활성 작성자");
        User deletedUser = saveUser("삭제 작성자");

        Book activeBook = saveBook("활성 리뷰 도서");
        Book deletedBook = saveBook("삭제 리뷰 도서");

        Review activeReview = saveReview(
                activeUser,
                activeBook,
                "활성 리뷰",
                5
        );

        Review deletedReview = Review.create(
                deletedUser,
                deletedBook,
                "삭제 리뷰",
                4
        );
        deletedReview.softDelete();
        reviewRepository.saveAndFlush(deletedReview);

        ReviewSearchRequest request = new ReviewSearchRequest(
                null,
                null,
                null,
                "createdAt",
                "DESC",
                null,
                null,
                50
        );

        List<Review> result =
                reviewRepository.findAllByCursor(request);

        long totalElements =
                reviewRepository.countAll(request);

        assertThat(result)
                .extracting(Review::getId)
                .containsExactly(activeReview.getId());

        assertThat(totalElements).isEqualTo(1L);
    }

    @Test
    void 평점이_같으면_생성일_보조_커서_다음의_리뷰를_조회한다() {
        User newestUser = saveUser("최신 작성자");
        User cursorUser = saveUser("커서 작성자");
        User oldestUser = saveUser("이전 작성자");

        Book newestBook = saveBook("최신 도서");
        Book cursorBook = saveBook("커서 도서");
        Book oldestBook = saveBook("이전 도서");

        Review newestReview = saveReview(
                newestUser,
                newestBook,
                "최신 리뷰",
                4
        );

        Review cursorReview = saveReview(
                cursorUser,
                cursorBook,
                "커서 리뷰",
                4
        );

        Review oldestReview = saveReview(
                oldestUser,
                oldestBook,
                "이전 리뷰",
                4
        );

        LocalDateTime newestCreatedAt =
                LocalDateTime.of(2026, 8, 21, 11, 0);

        LocalDateTime cursorCreatedAt =
                LocalDateTime.of(2026, 8, 21, 10, 0);

        LocalDateTime oldestCreatedAt =
                LocalDateTime.of(2026, 8, 21, 9, 0);

        updateCreatedAt(
                newestReview.getId(),
                newestCreatedAt
        );

        updateCreatedAt(
                cursorReview.getId(),
                cursorCreatedAt
        );

        updateCreatedAt(
                oldestReview.getId(),
                oldestCreatedAt
        );

        entityManager.clear();

        ReviewSearchRequest request = new ReviewSearchRequest(
                null,
                null,
                null,
                "rating",
                "DESC",
                ReviewCursor.encode(
                        cursorReview.getRating(),
                        cursorReview.getId()
                ),
                cursorCreatedAt,
                50
        );

        List<Review> result =
                reviewRepository.findAllByCursor(request);

        assertThat(result)
                .extracting(Review::getId)
                .containsExactly(oldestReview.getId());
    }

    @Test
    void 생성일이_같아도_리뷰_ID_커서_다음의_리뷰를_누락하지_않는다() {
        User user1 = saveUser("동시 작성자 1");
        User user2 = saveUser("동시 작성자 2");
        User user3 = saveUser("동시 작성자 3");

        Book book1 = saveBook("동시 도서 1");
        Book book2 = saveBook("동시 도서 2");
        Book book3 = saveBook("동시 도서 3");

        Review review1 =
                saveReview(user1, book1, "동시 리뷰 1", 5);

        Review review2 =
                saveReview(user2, book2, "동시 리뷰 2", 4);

        Review review3 =
                saveReview(user3, book3, "동시 리뷰 3", 3);

        LocalDateTime sameCreatedAt =
                LocalDateTime.of(2026, 8, 21, 10, 0);

        updateCreatedAt(review1.getId(), sameCreatedAt);
        updateCreatedAt(review2.getId(), sameCreatedAt);
        updateCreatedAt(review3.getId(), sameCreatedAt);

        entityManager.clear();

        ReviewSearchRequest initialRequest = new ReviewSearchRequest(
                null,
                null,
                null,
                "createdAt",
                "DESC",
                null,
                null,
                50
        );

        List<Review> orderedReviews =
                reviewRepository.findAllByCursor(initialRequest);

        Review cursorReview = orderedReviews.get(0);

        ReviewSearchRequest cursorRequest = new ReviewSearchRequest(
                null,
                null,
                null,
                "createdAt",
                "DESC",
                ReviewCursor.encode(
                        cursorReview.getCreatedAt(),
                        cursorReview.getId()
                ),
                cursorReview.getCreatedAt(),
                50
        );

        List<Review> result =
                reviewRepository.findAllByCursor(cursorRequest);

        assertThat(result)
                .extracting(Review::getId)
                .containsExactly(
                        orderedReviews.get(1).getId(),
                        orderedReviews.get(2).getId()
                );
    }

    @Test
    void 평점과_생성일이_같아도_리뷰_ID_커서_다음의_리뷰를_누락하지_않는다() {
        User user1 = saveUser("동점 작성자 1");
        User user2 = saveUser("동점 작성자 2");
        User user3 = saveUser("동점 작성자 3");

        Book book1 = saveBook("동점 도서 1");
        Book book2 = saveBook("동점 도서 2");
        Book book3 = saveBook("동점 도서 3");

        Review review1 =
                saveReview(user1, book1, "동점 리뷰 1", 4);

        Review review2 =
                saveReview(user2, book2, "동점 리뷰 2", 4);

        Review review3 =
                saveReview(user3, book3, "동점 리뷰 3", 4);

        LocalDateTime sameCreatedAt =
                LocalDateTime.of(2026, 8, 21, 10, 0);

        updateCreatedAt(review1.getId(), sameCreatedAt);
        updateCreatedAt(review2.getId(), sameCreatedAt);
        updateCreatedAt(review3.getId(), sameCreatedAt);

        entityManager.clear();

        ReviewSearchRequest initialRequest = new ReviewSearchRequest(
                null,
                null,
                null,
                "rating",
                "DESC",
                null,
                null,
                50
        );

        List<Review> orderedReviews =
                reviewRepository.findAllByCursor(initialRequest);

        Review cursorReview = orderedReviews.get(0);

        ReviewSearchRequest cursorRequest = new ReviewSearchRequest(
                null,
                null,
                null,
                "rating",
                "DESC",
                ReviewCursor.encode(
                        cursorReview.getRating(),
                        cursorReview.getId()
                ),
                cursorReview.getCreatedAt(),
                50
        );

        List<Review> result =
                reviewRepository.findAllByCursor(cursorRequest);

        assertThat(result)
                .extracting(Review::getId)
                .containsExactly(
                        orderedReviews.get(1).getId(),
                        orderedReviews.get(2).getId()
                );
    }

    private void updateCreatedAt(
            UUID reviewId,
            LocalDateTime createdAt
    ) {
        entityManager.createQuery(
                        """
                        UPDATE Review review
                        SET review.createdAt = :createdAt
                        WHERE review.id = :reviewId
                        """
                )
                .setParameter("createdAt", createdAt)
                .setParameter("reviewId", reviewId)
                .executeUpdate();
    }

    private User saveUser(String nickname) {
        return userRepository.save(
                User.create(
                        UUID.randomUUID() + "@example.com",
                        nickname,
                        "encodedPassword"
                )
        );
    }

    private Book saveBook(String title) {
        return bookRepository.save(
                new Book(
                        title,
                        "테스트 저자",
                        "테스트 설명",
                        "테스트 출판사",
                        LocalDate.of(2026, 8, 21),
                        null
                )
        );
    }

    private Review saveReview(
            User user,
            Book book,
            String content,
            int rating
    ) {
        return reviewRepository.saveAndFlush(
                Review.create(
                        user,
                        book,
                        content,
                        rating
                )
        );
    }
}
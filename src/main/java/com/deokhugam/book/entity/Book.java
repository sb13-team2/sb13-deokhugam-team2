package com.deokhugam.book.entity;

import com.deokhugam.global.entity.SoftDeleteEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "books",
    indexes = {
        @Index(
            name = "idx_book_title_created_at",
            columnList = "title, created_at"
        ),
        @Index(
            name = "idx_book_published_date_created_at",
            columnList = "published_date, created_at"
        )
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Book extends SoftDeleteEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(nullable = false, length = 150)
  private String title;

  @Column(nullable = false, length = 50)
  private String author;

  @Column(length = 1000)
  private String description;

  @Column(nullable = false, length = 50)
  private String publisher;

  @Column(nullable = false)
  private LocalDate publishedDate;

  @Column(unique = true, length = 13)
  private String isbn;

  private String thumbnailUrl;

  public Book(
      String title,
      String author,
      String description,
      String publisher,
      LocalDate publishedDate,
      String isbn
  ) {
    this.title = title;
    this.author = author;
    this.description = description;
    this.publisher = publisher;
    this.publishedDate = publishedDate;
    this.isbn = isbn;
  }

  public void update(
      String title,
      String author,
      String description,
      String publisher,
      LocalDate publishedDate
  ) {
    this.title = title;
    this.author = author;
    this.description = description;
    this.publisher = publisher;
    this.publishedDate = publishedDate;
  }

  public void updateThumbnailUrl(String thumbnailUrl) {
    this.thumbnailUrl = thumbnailUrl;
  }

}

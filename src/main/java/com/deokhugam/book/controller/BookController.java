package com.deokhugam.book.controller;

import com.deokhugam.book.dto.request.BookCreateRequest;
import com.deokhugam.book.dto.request.BookSearchRequest;
import com.deokhugam.book.dto.request.BookUpdateRequest;
import com.deokhugam.book.dto.response.BookDto;
import com.deokhugam.book.dto.response.CursorPageResponse;
import com.deokhugam.book.service.BasicBookService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {

  private final BasicBookService bookService;

  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<BookDto> create(
      @RequestPart("bookData") BookCreateRequest request,
      @RequestPart(value = "thumbnailImage", required = false) MultipartFile thumbnailImage) {

    BookDto bookDto = bookService.create(request, thumbnailImage);

    return ResponseEntity.status(HttpStatus.CREATED).body(bookDto);
  }

  @GetMapping("/{bookId}")
  public ResponseEntity<BookDto> findById(
      @PathVariable UUID bookId) {

    BookDto bookDto = bookService.findById(bookId);

    return ResponseEntity.ok(bookDto);
  }

  @GetMapping
  public ResponseEntity<CursorPageResponse<BookDto>> findAll(
      @ModelAttribute BookSearchRequest request
  ) {
    CursorPageResponse<BookDto> response = bookService.findAll(request);

    return ResponseEntity.ok(response);
  }

  @PatchMapping(value = "/{bookId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<BookDto> update(
      @PathVariable UUID bookId,
      @RequestPart("bookData") BookUpdateRequest request,
      @RequestPart(value = "thumbnailImage", required = false) MultipartFile thumbnailImage
  ) {
    BookDto bookDto = bookService.update(bookId, request, thumbnailImage);

    return ResponseEntity.ok(bookDto);
  }

  @DeleteMapping("/{bookId}")
  public ResponseEntity<Void> delete(
      @PathVariable UUID bookId
  ) {
    bookService.delete(bookId);

    return ResponseEntity.noContent().build();
  }

}

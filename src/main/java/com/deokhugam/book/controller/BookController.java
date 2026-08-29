package com.deokhugam.book.controller;

import com.deokhugam.book.controller.doc.BookControllerDoc;
import com.deokhugam.book.dto.request.BookCreateRequest;
import com.deokhugam.book.dto.request.BookSearchRequest;
import com.deokhugam.book.dto.request.BookUpdateRequest;
import com.deokhugam.book.dto.response.BookDto;
import com.deokhugam.book.dto.response.BookInfoResponse;
import com.deokhugam.book.dto.response.CursorPageResponse;
import com.deokhugam.book.service.BookService;
import jakarta.validation.Valid;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController implements BookControllerDoc {

  private final BookService bookService;

  @Override
  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<BookDto> create(
      @RequestPart("bookData") @Valid BookCreateRequest request,
      @RequestPart(value = "thumbnailImage", required = false) MultipartFile thumbnailImage) {

    BookDto bookDto = bookService.create(request, thumbnailImage);

    return ResponseEntity.status(HttpStatus.CREATED).body(bookDto);
  }

  @Override
  @PostMapping(
      value = "/isbn/ocr",
      consumes = MediaType.MULTIPART_FORM_DATA_VALUE
  )
  public ResponseEntity<String> extractIsbn(
      @RequestPart("image") MultipartFile image
  ) {
    String isbn = bookService.extractIsbnFromImage(image);

    return ResponseEntity.ok(isbn);
  }

  @Override
  @GetMapping("/{bookId}")
  public ResponseEntity<BookDto> findById(
      @PathVariable UUID bookId) {

    BookDto bookDto = bookService.findById(bookId);

    return ResponseEntity.ok(bookDto);
  }

  @Override
  @GetMapping
  public ResponseEntity<CursorPageResponse<BookDto>> findAll(
      @Valid @ModelAttribute BookSearchRequest request
  ) {
    CursorPageResponse<BookDto> response = bookService.findAll(request);

    return ResponseEntity.ok(response);
  }

  @Override
  @PatchMapping(value = "/{bookId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<BookDto> update(
      @PathVariable UUID bookId,
      @RequestPart("bookData") @Valid BookUpdateRequest request,
      @RequestPart(value = "thumbnailImage", required = false) MultipartFile thumbnailImage
  ) {
    BookDto bookDto = bookService.update(bookId, request, thumbnailImage);

    return ResponseEntity.ok(bookDto);
  }

  @Override
  @DeleteMapping("/{bookId}")
  public ResponseEntity<Void> delete(
      @PathVariable UUID bookId
  ) {
    bookService.delete(bookId);

    return ResponseEntity.noContent().build();
  }

  @Override
  @DeleteMapping("/{bookId}/hard")
  public ResponseEntity<Void> hardDelete(
      @PathVariable UUID bookId
  ) {
    bookService.hardDelete(bookId);
    return ResponseEntity.noContent().build();
  }

  @Override
  @GetMapping("/info")
  public ResponseEntity<BookInfoResponse> findBookInfoByIsbn(
      @RequestParam String isbn) {
    return ResponseEntity.ok(bookService.findBookInfoByIsbn(isbn));
  }

}

package com.deokhugam.book.mapper;

import com.deokhugam.book.dto.request.BookCreateRequest;
import com.deokhugam.book.dto.response.BookDto;
import com.deokhugam.book.entity.Book;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BookMapper {

  default Book toEntity(BookCreateRequest request) {
    if (request == null) {
      return null;
    }

    return new Book(
        request.title(),
        request.author(),
        request.description(),
        request.publisher(),
        request.publishedDate(),
        request.isbn()
    );
  }

  @Mapping(target = "reviewCount", constant = "0")
  @Mapping(target = "rating", constant = "0.0")
  BookDto toDto(Book book);

}

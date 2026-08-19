package com.deokhugam.book.dto.response;

import com.deokhugam.book.entity.Book;

public record BookSearchResult(
    Book book,
    long reviewCount,
    double rating
) {

}

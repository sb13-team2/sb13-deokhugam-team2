package com.deokhugam.book.dto.request;

import java.time.LocalDate;

public record BookUpdateRequest(

    String title,
    String author,
    String description,
    String publisher,
    LocalDate publisherDate
) {

}

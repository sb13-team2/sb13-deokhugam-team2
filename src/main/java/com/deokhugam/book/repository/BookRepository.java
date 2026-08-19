package com.deokhugam.book.repository;

import com.deokhugam.book.entity.Book;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRepository extends JpaRepository<Book, UUID>, BookRepositoryCustom {

  boolean existsByIsbn(String isbn);

  Optional<Book> findByIdAndDeletedAtIsNull(UUID id);

}

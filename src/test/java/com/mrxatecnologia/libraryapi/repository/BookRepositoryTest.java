package com.mrxatecnologia.libraryapi.repository;

import com.mrxatecnologia.libraryapi.dto.BookDTO;
import com.mrxatecnologia.libraryapi.entity.Book;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Description;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@DataJpaTest
public class BookRepositoryTest {

    @Autowired
    TestEntityManager entityManager;

    @Autowired
    BookRepository repository;

    @Test
    @Description("Deve retornar verdadeiro quando existir um livro na base com o isbn informado")
    public void returnTrueWhemIsbnExists () {
        //Cenário
        String isbn = "123";

        Book book = Book.builder().title("As aventuras").author("Fulano").isbn(isbn).build();
        entityManager.persist(book);

        //Execução
        Boolean exists = repository.existsByIsbn(isbn);

        //Verificação

        assertThat(exists).isTrue();
    }

    @Test
    @Description("Deve retornar verdadeiro quando existir um livro na base com o isbn informado")
    public void returnFalseWhemIsbnDoesNotExists () {
        //Cenário
        String isbn = "123";

        //Execução
        Boolean exists = repository.existsByIsbn(isbn);

        //Verificação

        assertThat(exists).isFalse();
    }

    @Test
    @Description("Deve salvar um livro")
    public void saveBookTest() {
        Book book = Book.builder().isbn("123").build();
        Book savedBook =  repository.save(book);
        assertThat(savedBook.getId()).isNotNull();
    }

    @Test
    @Description("Deve deletar um livro")
    public void deleteBookTest() {
       Book book = Book.builder().isbn("123").build();
       entityManager.persist(book);
       Book foundBook = entityManager.find(Book.class, book.getId());
       repository.delete(foundBook);
       Book deletedBook = entityManager.find(Book.class, book.getId());
       assertThat(deletedBook).isNull();

    }

}

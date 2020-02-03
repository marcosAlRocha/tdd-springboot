package com.mrxatecnologia.libraryapi.service;


import com.mrxatecnologia.libraryapi.entity.Book;
import com.mrxatecnologia.libraryapi.exceptions.BusinessException;
import com.mrxatecnologia.libraryapi.repository.BookRepository;
import com.mrxatecnologia.libraryapi.service.Impl.BookServiceImpl;
import org.aspectj.lang.annotation.Before;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class BookServiceTest {

    BookService service;

    @MockBean
    BookRepository repository;

    @BeforeEach
    public void setUp() {
         this.service = new BookServiceImpl( repository );
    }

    @Test
    @DisplayName("Deve salvar um livro")
    public void saveBootTest () {
        //cenário
        Book book = createValidBook();
        when(repository.existsByIsbn(Mockito.anyString())).thenReturn(false);
        when(repository.save(book)).thenReturn(
                Book.builder()
                        .id(11L)
                        .isbn("123")
                        .author("Fulano")
                        .title("As aventuras").build()
        );

        //execução
        Book savedBook = service.save(book);

        //verificação
        assertThat(savedBook.getId()).isNotNull();
        assertThat(savedBook.getIsbn()).isEqualTo("123");
        assertThat(savedBook.getTitle()).isEqualTo("As aventuras");
        assertThat(savedBook.getAuthor()).isEqualTo("Fulano");
    }

    public static Book createValidBook() {
        return Book.builder().isbn("123").author("Fulano").title("As aventuras").build();
    }

    @Test
    @DisplayName("Deve lançar erro de negócio ao tentar salvar um livro com isbn duplicado")
    public void shouldNotSaveABookWithDuplicateISBN() {
        //cenário
        Book book = createValidBook();
        when(repository.existsByIsbn(Mockito.anyString())).thenReturn(true);

        Throwable exception = Assertions.catchThrowable(() -> service.save(book));
        assertThat(exception)
                .isInstanceOf(BusinessException.class)
                .hasMessage("Isbn já cadastrado");

        verify(repository, Mockito.never()).save(book);

    }

    @Test
    @DisplayName("Deve obter um livro")
    public void getByIdTest() {
        //Cenário
        Long id = 1l;
        Book book = createValidBook();
        book.setId(id);
        when(repository.findById(id)).thenReturn(Optional.of(book));

        //execução
        Optional<Book> foundBook = service.getById(id);

        //verificação
        assertThat(foundBook.isPresent()).isTrue();
        assertThat(foundBook.get().getId()).isEqualTo(id);
        assertThat(foundBook.get().getAuthor()).isEqualTo(book.getAuthor());
        assertThat(foundBook.get().getIsbn()).isEqualTo(book.getIsbn());
        assertThat(foundBook.get().getTitle()).isEqualTo(book.getTitle());

     }

    @Test
    @DisplayName("Deve retornar vazio ao obter um livro por id quando ele não existe na base ")
    public void bookNotFoundTest() {
        //Cenário
        Long id = 1l;

        when(repository.findById(id)).thenReturn(Optional.empty());

        //execução
        Optional<Book> foundBook = service.getById(id);

        //verificação
        assertThat(foundBook.isPresent()).isFalse();

    }

    @Test
    @DisplayName("Deve deletar um livro com id válido")
    public void deleteValidBook() throws Exception {
        //Cenário
        Long id = 1l;
        Book book = Book.builder().id(id).build();

        //Execução
        assertDoesNotThrow( () -> service.delete(book));

        //Verificação

        verify(repository, times(1)).delete(book);
    }

    @Test
    @DisplayName("Deve retornar erro 404 quando o livro não existir na base de dados")
    public void inexistentIdDeleteTest () {
        //Cenário
        Book book = new Book();

        //Execução
        assertThrows(IllegalArgumentException.class, () -> service.delete(book));

        //Verificação
        verify(repository, Mockito.never()).delete(book);

    }

    @Test
    @DisplayName("Deve retornar erro 404 quando o livro não existir na base de dados")
    public void updateInvalidBookTest () {
        //Cenário
        Book book = new Book();

        //Execução
        assertThrows(IllegalArgumentException.class, () -> service.update(book));

        //Verificação
        verify(repository, Mockito.never()).save(book);

    }

    @Test
    @DisplayName("Deve atualizar um livro com id válido")
    public void updateBookTest() throws Exception {
        //Cenário
        Long id = 1l;
        Book updatingBook = Book.builder().id(id).build();
        Book updateBook = createValidBook();
        updateBook.setId(id);
        when(repository.save(updatingBook)).thenReturn(updateBook);
        //Execução

        Book book = service.update(updatingBook);


        //Verificação
        assertThat(book.getId()).isEqualTo(updateBook.getId());
        assertThat(book.getTitle()).isEqualTo(updateBook.getTitle());
        assertThat(book.getIsbn()).isEqualTo(updateBook.getIsbn());
        assertThat(book.getAuthor()).isEqualTo(updateBook.getAuthor());

    }

    @Test
    @DisplayName("Deve filtrar livros pelas propriedades")
    public void findBookTest() {
        Book book = createValidBook();

        PageRequest pageRequest = PageRequest.of(0, 10);

        List<Book> lista = Arrays.asList(book);
        Page<Book> page = new PageImpl<Book>(lista, pageRequest, 1);
        when( repository.findAll(Mockito.any(Example.class), Mockito.any(PageRequest.class)))
                .thenReturn(page);

        Page<Book> result = service.find(book, pageRequest);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).isEqualTo(lista);
        assertThat(result.getPageable().getPageNumber()).isEqualTo(0);
        assertThat(result.getPageable().getPageSize()).isEqualTo(10);
    }

    @Test
    @DisplayName("Deve obter um livro pelo isbn")
    public void getBookByIsbn() {
        String isbn = "1230";
        when(repository.findByIsbn(isbn)).thenReturn(Optional.of(Book.builder().id(1L).isbn(isbn).build()));

        Optional<Book> book = service.getBookByIsbn(isbn);

        assertThat(book.isPresent()).isTrue();
        assertThat(book.get().getId()).isEqualTo(1L);
        assertThat(book.get().getIsbn()).isEqualTo(isbn);

        verify(repository, times(1)).findByIsbn(isbn);
    }




    @Test
    @DisplayName("Deve lançar erro de validação quando não houver dados suficientes para criação do livro")
    public void createInvalidBootTest() throws Exception {

    }


}

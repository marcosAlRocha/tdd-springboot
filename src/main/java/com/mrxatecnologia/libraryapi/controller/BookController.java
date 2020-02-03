package com.mrxatecnologia.libraryapi.controller;

import com.mrxatecnologia.libraryapi.dto.BookDTO;
import com.mrxatecnologia.libraryapi.dto.LoanDTO;
import com.mrxatecnologia.libraryapi.entity.Book;
import com.mrxatecnologia.libraryapi.entity.Loan;
import com.mrxatecnologia.libraryapi.service.BookService;
import com.mrxatecnologia.libraryapi.service.LoanService;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@Log4j2
@AllArgsConstructor
@RestController
@RequestMapping("/api/books")
public class BookController {

    private final BookService bookService;

    private final ModelMapper modelMapper;

    private final LoanService loanService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookDTO create(@RequestBody @Valid BookDTO dto) {
        log.info("Novo livro criado novo livro com isbn:{}", dto.getIsbn());
        Book entity = modelMapper.map(dto, Book.class);
        entity = bookService.save(entity);
        return modelMapper.map(entity, BookDTO.class);
    }

    @GetMapping("{id}")
    public BookDTO get(@PathVariable Long id) {
        log.info("Obtido livro com isbn:{}", id);
        return bookService.
                getById(id)
                .map(book -> modelMapper.map(book, BookDTO.class))
                .orElseThrow(() -> new ResponseStatusException((HttpStatus.NOT_FOUND)));
    }

    @DeleteMapping("{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        log.info("Excluído livro com id:{}", id);
        Book book = bookService.getById(id).orElseThrow(() -> new ResponseStatusException((HttpStatus.NOT_FOUND)));
        bookService.delete(book);
    }

    @PutMapping("{id}")
    public BookDTO udpate(@PathVariable long id, @RequestBody @Valid BookDTO bookDTO) {

        log.info("Atualizando livro com isbn:{}", bookDTO.getIsbn());

        return  bookService.getById(id).map( book -> {

            book.setAuthor(bookDTO.getAuthor());
            book.setTitle(bookDTO.getTitle());
            book = bookService.update(book);
            return modelMapper.map(book, BookDTO.class);

        }).orElseThrow( () -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @GetMapping
    public Page<BookDTO> find(BookDTO bookDTO, Pageable pageRequest) {
        Book filter = modelMapper.map(bookDTO, Book.class);
        Page<Book> result = bookService.find(filter, pageRequest);
        List<BookDTO> list = result.getContent()
                .stream()
                .map(entity -> modelMapper.map(entity, BookDTO.class))
                .collect(Collectors.toList());
        return new PageImpl<BookDTO>(list, pageRequest, result.getTotalElements());
    }

    @GetMapping("{id}/loans")
    public Page<LoanDTO> loansByBook(@PathVariable Long id, Pageable pageable) {
        Book book = bookService.getById(id).orElseThrow( () -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        Page<Loan> result = loanService.getLoansByBook(book, pageable);
        List<LoanDTO> list = result.getContent()
                .stream()
                .map(loan->
                {
                    Book loanBook = loan.getBook();
                    BookDTO bookDTO = modelMapper.map(loanBook, BookDTO.class);
                    LoanDTO loanDTO = modelMapper.map(loan, LoanDTO.class);
                    loanDTO.setBook(bookDTO);
                    return loanDTO;
                }).collect(Collectors.toList());
        return new PageImpl<LoanDTO>(list, pageable, result.getTotalElements());
    }
}

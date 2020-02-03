package com.mrxatecnologia.libraryapi.controller;

import com.mrxatecnologia.libraryapi.dto.BookDTO;
import com.mrxatecnologia.libraryapi.dto.LoanDTO;
import com.mrxatecnologia.libraryapi.dto.LoanFilterDTO;
import com.mrxatecnologia.libraryapi.dto.ReturnedLoanDTO;
import com.mrxatecnologia.libraryapi.entity.Book;
import com.mrxatecnologia.libraryapi.entity.Loan;
import com.mrxatecnologia.libraryapi.service.BookService;
import com.mrxatecnologia.libraryapi.service.LoanService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/loans")
@RequiredArgsConstructor
public class LoanController {

    private final LoanService loanService;
    private final BookService bookService;
    private final ModelMapper modelMapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Long create(@RequestBody LoanDTO dto) {

        Book book = bookService.
                getBookByIsbn(dto.getIsbn())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Book not found for passed isbn"));
        Loan entity = Loan.builder()
                .book(book)
                .customer(dto.getCustomer())
                .loanDate(LocalDate.now())
                .build();

        entity = loanService.save(entity);

        return entity.getId();
    }

    @PatchMapping("{id}")
    public void returnBook(@PathVariable Long id, @RequestBody ReturnedLoanDTO dto) {
        Loan loan = loanService.getById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        loan.setReturned(dto.getReturned());
        loanService.update(loan);
    }

    @GetMapping
    public Page<LoanDTO> find(LoanFilterDTO dto, Pageable pageable) {

        Page<Loan> result = loanService.find(dto, pageable);
        List<LoanDTO> loans = result
                .getContent()
                .stream()
                .map( entidade -> {
                    Book book = entidade.getBook();
                    BookDTO bookDTO = modelMapper.map(book, BookDTO.class);
                    LoanDTO loanDTO = modelMapper.map(entidade, LoanDTO.class);
                    loanDTO.setBook(bookDTO);
                    return loanDTO;
                }).collect(Collectors.toList());
            return new PageImpl<LoanDTO>(loans, pageable, result.getTotalElements());
    }

}

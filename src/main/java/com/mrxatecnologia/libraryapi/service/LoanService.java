package com.mrxatecnologia.libraryapi.service;

import com.mrxatecnologia.libraryapi.dto.LoanFilterDTO;
import com.mrxatecnologia.libraryapi.entity.Book;
import com.mrxatecnologia.libraryapi.entity.Loan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface LoanService {

    Loan save(Loan any);

    Optional<Loan> getById(Long id);

    Loan update(Loan loan);

    Page<Loan> find (LoanFilterDTO filterDTO, Pageable pageable);

    Page<Loan> getLoansByBook(Book book, Pageable pageable);

    List<Loan> getAllLateLoans();

}

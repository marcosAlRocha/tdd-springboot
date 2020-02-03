package com.mrxatecnologia.libraryapi.service;

import com.mrxatecnologia.libraryapi.dto.LoanFilterDTO;
import com.mrxatecnologia.libraryapi.entity.Book;
import com.mrxatecnologia.libraryapi.entity.Loan;
import com.mrxatecnologia.libraryapi.exceptions.BusinessException;
import com.mrxatecnologia.libraryapi.repository.LoanRepository;
import com.mrxatecnologia.libraryapi.service.Impl.LoanServiceImpl;
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

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class LoanServiceTest {

    LoanService loanService;

    @MockBean
    LoanRepository loanRepository;

    @BeforeEach
    public void setUp() {
        this.loanService = new LoanServiceImpl(loanRepository);
    }

    @Test
    @DisplayName("Deve salvar um empréstimo")
    public void saveLoanTest() {

        Book book = Book.builder().id(1L).build();
        String customer = "Fulano";

        Loan savingLoan = Loan.builder()
                .book(book)
                .customer(customer)
                .loanDate(LocalDate.now())
                .build();

        Loan savedLoad = Loan.builder()
                .id(1L)
                .loanDate(LocalDate.now())
                .customer(customer)
                .book(book).build();

        when(loanRepository.existsByBookAndNotReturned(book)).thenReturn(false);
        when (loanRepository.save(savingLoan)).thenReturn(savedLoad);

        Loan loan = loanService.save(savingLoan);

        assertThat(loan.getId()).isEqualTo(savedLoad.getId());
        assertThat(loan.getBook().getId()).isEqualTo(savedLoad.getBook().getId());
        assertThat(loan.getCustomer()).isEqualTo(savedLoad.getCustomer());
        assertThat(loan.getLoanDate()).isEqualTo(savedLoad.getLoanDate());

    }

    @Test
    @DisplayName("Deve lançar erro de negócio ao salvar um empréstimo com livro já emprestado")
    public void loanedBookSaveTest() {

        Book book = Book.builder().id(1L).build();
        String customer = "Fulano";

        Loan savingLoan = Loan.builder()
                .book(book)
                .customer(customer)
                .loanDate(LocalDate.now())
                .build();


        when(loanRepository.existsByBookAndNotReturned(book)).thenReturn(true);
        Throwable exception =  catchThrowable( () -> loanService.save(savingLoan));

        assertThat(exception)
                .isInstanceOf(BusinessException.class)
                .hasMessage("Book already loaned");


        verify(loanRepository, never()).save(savingLoan);

    }

    @Test
    @DisplayName("Deve obter as informações de um empréstimo pelo ID")
    public void getLoanDetailsTest() {

        Long id = 1L;
        Loan loan = createLoan();
        loan.setId(1L);

        Mockito.when(loanRepository.findById(id)).thenReturn(Optional.of(loan));

        Optional<Loan> result = loanService.getById(id);

        assertThat(result.isPresent()).isTrue();
        assertThat(result.get().getId()).isEqualTo(id);
        assertThat(result.get().getCustomer()).isEqualTo(loan.getCustomer());
        assertThat(result.get().getBook()).isEqualTo(loan.getBook());
        assertThat(result.get().getLoanDate()).isEqualTo(loan.getLoanDate());

        verify(loanRepository).findById(id);

    }

    @Test
    @DisplayName("Deve atualizar um empréstimo")
    public void updateLoanTest() {
        Loan loan = createLoan();
        loan.setId(1L);
        loan.setReturned(true);

        when(loanRepository.save(loan)).thenReturn(loan);

        Loan updateLoan = loanService.update(loan);

        assertThat(updateLoan.getReturned()).isTrue();
        verify(loanRepository).save(loan);
    }


    @Test
    @DisplayName("Deve filtrar empréstimos pelas propriedades")
    public void findLoanTest() {

        LoanFilterDTO loanFilterDTO = LoanFilterDTO.builder().isbn("123").customer("Fulano").build();

        Loan loan = createLoan();
        loan.setId(1L);
        PageRequest pageRequest = PageRequest.of(0, 10);
        List<Loan> lista = Arrays.asList(loan);

        Page<Loan> page = new PageImpl<Loan>(lista, pageRequest, lista.size());

        when( loanRepository.findByBookIsbnOrCustomer(
                Mockito.anyString(),
                Mockito.anyString(),
                Mockito.any(PageRequest.class)))
                .thenReturn(page);

        Page<Loan> result = loanService.find(loanFilterDTO, pageRequest);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).isEqualTo(lista);
        assertThat(result.getPageable().getPageNumber()).isEqualTo(0);
        assertThat(result.getPageable().getPageSize()).isEqualTo(10);
    }

    public static Loan createLoan() {
        Book book = Book.builder().id(1L).build();
        String customer = "Fulano";

        return  Loan.builder()
                .book(book)
                .customer(customer)
                .loanDate(LocalDate.now())
                .build();
    }


}

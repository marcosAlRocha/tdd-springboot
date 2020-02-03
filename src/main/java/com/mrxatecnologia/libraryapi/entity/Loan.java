package com.mrxatecnologia.libraryapi.entity;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String customer;

    private String CustomerEmail;

    @JoinColumn(name = "id_book")
    @ManyToOne
    private Book book;

    private LocalDate loanDate;

    private Boolean returned;


}

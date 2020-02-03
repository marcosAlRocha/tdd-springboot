package com.mrxatecnologia.libraryapi.service;

import com.mrxatecnologia.libraryapi.entity.Loan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ScheduleService {

    private static final String CRON_LATE_LOANS = " 0 0 0 1/1 * ?";

    @Value("${application.mail.lateLoans.messages}")
    private String message;

    @Autowired
    LoanService loanService;

    @Autowired
    EmailService emailService;

    @Scheduled(cron = CRON_LATE_LOANS)
    public void sendEmailToLateLoans() {

        List<Loan> allLateLoans = loanService.getAllLateLoans();
        List<String> mailsList = allLateLoans.stream().map(loan -> loan.getCustomerEmail())
                .collect(Collectors.toList());


        emailService.sendEmails(message, mailsList);

    }

}

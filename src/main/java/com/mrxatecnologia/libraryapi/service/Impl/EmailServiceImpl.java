package com.mrxatecnologia.libraryapi.service.Impl;

import com.mrxatecnologia.libraryapi.service.EmailService;
import net.bytebuddy.implementation.bind.annotation.FieldValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmailServiceImpl implements EmailService {

    @Value("${application.mail.default.sender}")
    private String remetente;

    @Autowired
    JavaMailSender javaMailSender;

    @Override
    public void sendEmails(String message, List<String> mailsList) {
        String[] mails = mailsList.toArray(new String[mailsList.size()]);
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setFrom(remetente);
        mailMessage.setSubject("Livro com empreśtimos atrasado");
        mailMessage.setText(message);
        mailMessage.setTo(mails);
        javaMailSender.send(mailMessage);
    }
}

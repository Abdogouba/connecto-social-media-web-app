package com.socialmedia.connecto.services;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendPassword(String to, String tempPassword) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Password Reset - Connecto App");
        message.setText("Hello,\n\nYour new password is: " + tempPassword +
                "\n\nPlease log in and change your password as soon as possible.");

        mailSender.send(message);
    }
}


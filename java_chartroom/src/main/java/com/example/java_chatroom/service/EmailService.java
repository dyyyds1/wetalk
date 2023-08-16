package com.example.java_chatroom.service;

import com.example.java_chatroom.utils.VerificationCodeGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import javax.mail.internet.InternetAddress;

@Service
public class EmailService {
    private final JavaMailSender javaMailSender;

    @Autowired
    public EmailService(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    public void sendVerificationCode(String toEmail, String verificationCode) {
        String subject = "Your Verification Code";
        String body = "Your verification code is: " + verificationCode;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("wetalk2023@163.com");
        message.setTo(toEmail);
        message.setSubject(subject);
        message.setText(body);
        javaMailSender.send(message);
    }
}


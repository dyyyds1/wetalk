package com.example.java_chatroom.controller;

import com.example.java_chatroom.service.EmailService;
import com.example.java_chatroom.service.VerificationCodeCache;
import com.example.java_chatroom.utils.VerificationCodeGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class VerificationController {
    private final EmailService emailService;
    private final VerificationCodeCache verificationCodeCache;

    @Autowired
    public VerificationController(EmailService emailService, VerificationCodeCache verificationCodeCache) {
        this.emailService = emailService;
        this.verificationCodeCache = verificationCodeCache;
    }

    @PostMapping("/sendVerificationCode")
    public ResponseEntity<String> sendVerificationCode(String toEmail) {
        System.out.println(toEmail);
        String verificationCode = VerificationCodeGenerator.generateCode(6);
        // 将验证码保存到 Redis 缓存，有效期设置为5分钟（300秒）
        verificationCodeCache.saveCode(toEmail, verificationCode, 300);

        // 发送邮件包含验证码
        emailService.sendVerificationCode(toEmail, verificationCode);

        return ResponseEntity.ok("Verification code sent successfully.");
    }


}


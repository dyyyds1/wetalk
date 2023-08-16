package com.example.java_chatroom.model;

import lombok.Data;

@Data
public class VerificationCodeRequest {
    private String email; // 邮箱地址，用于接收邮件或验证码
    private String code;  // 验证码，可以在发送验证码请求时填入，或者在验证验证码时从前端获取

    // 构造方法、getter和setter等省略，根据需要自行添加
}

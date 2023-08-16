package com.example.java_chatroom.service;

public interface VerificationCodeCache {
    void saveCode(String email, String code, long expirationInSeconds);

    boolean isCodeValid(String email, String code);
}

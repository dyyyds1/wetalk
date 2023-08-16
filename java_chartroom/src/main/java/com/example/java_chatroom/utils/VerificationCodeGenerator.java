package com.example.java_chatroom.utils;

import java.util.Random;

public class VerificationCodeGenerator {
    public static String generateCode(int length) {
        StringBuilder code = new StringBuilder();
        String characters = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        Random random = new Random();

        for (int i = 0; i < length; i++) {
            int index = random.nextInt(characters.length());
            code.append(characters.charAt(index));
        }

        return code.toString();
    }
}

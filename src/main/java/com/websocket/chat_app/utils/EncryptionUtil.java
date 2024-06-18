package com.websocket.chat_app.utils;

import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;

/**
 * Created by Obiora on 17-Jun-2024 at 12:00
 */

public class EncryptionUtil {
    private static final String SECRET_KEY = "mysecretkey12345678"; // 16 characters for AES-128
    private static final String SALT = "1234567890123456"; // 16 characters for AES-128

    private static final TextEncryptor encryptor = Encryptors.text(SECRET_KEY, SALT);

    public static String encrypt(String text) {
        return encryptor.encrypt(text);
    }

    public static String decrypt(String encryptedText) {
        return encryptor.decrypt(encryptedText);
    }
}

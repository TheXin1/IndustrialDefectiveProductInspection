package com.example.industrialdefectiveproductinspection.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public final class PasswordUtils {
    private static final BCryptPasswordEncoder ENCODER = new BCryptPasswordEncoder();

    private PasswordUtils() {
    }

    public static String hash(String rawPassword) {
        return ENCODER.encode(rawPassword);
    }

    public static boolean matches(String rawPassword, String encodedPassword) {
        return ENCODER.matches(rawPassword, encodedPassword);
    }
}

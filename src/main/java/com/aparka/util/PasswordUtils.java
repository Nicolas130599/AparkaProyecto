package com.aparka.util;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtils {
    public static String encriptar(String passwordPlana) {
        return BCrypt.hashpw(passwordPlana, BCrypt.gensalt());
    }

    public static boolean verificar(String passwordPlana, String passwordHash) {
        if (passwordHash == null || !passwordHash.startsWith("$2a$")) {
            return false;
        }
        return BCrypt.checkpw(passwordPlana, passwordHash);
    }
}
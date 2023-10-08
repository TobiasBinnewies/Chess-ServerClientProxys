package org.example.utils;

public class RandomStringGenerator {
    public static String generateRandomString(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int randomIndex = (int) (Math.random() * chars.length());
            stringBuilder.append(chars.charAt(randomIndex));
        }
        return stringBuilder.toString();
    }
}

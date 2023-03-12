package org.stransball;

import java.util.Base64;

public class Level {
    public String fileName;
    public String title;
    public String password;
    public int fuel;

    public Level(String fileName, String title, int fuel) {
        this.fileName = fileName;
        this.title = title;
        this.fuel = fuel;
    }

    public Level(String fileName, String title, int fuel, String password) {
        this(fileName, title, fuel);
        this.password = decode(password);
    }

    // No secrecy. Just trying no to spoil the passowrd
    private static String decode(String encodedString) {
        byte[] decodedBytes = Base64.getDecoder().decode(encodedString);
        return new String(decodedBytes);
    }
}
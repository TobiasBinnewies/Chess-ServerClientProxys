package org.example.exceptions;

public class PlayerException extends Exception{
    public PlayerException(String message) {
        super(message);
    }

    public int getErrorCode() {
        return 2;
    }
}

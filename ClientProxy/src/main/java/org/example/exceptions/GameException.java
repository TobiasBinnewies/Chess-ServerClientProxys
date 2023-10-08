package org.example.exceptions;

public class GameException extends Exception{
    public GameException(String message) {
        super(message);
    }

    public int getErrorCode() {
        return 1;
    }
}

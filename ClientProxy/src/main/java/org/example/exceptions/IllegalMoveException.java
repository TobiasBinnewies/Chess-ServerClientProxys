package org.example.exceptions;

public class IllegalMoveException extends Exception {
    public IllegalMoveException(String message) {
        super(message);
    }

    public int getErrorCode() {
        return 3;
    }
}

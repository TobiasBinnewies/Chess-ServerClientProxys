package org.example.exceptions;

public class JsonException extends Exception{
    public JsonException(String message) {
        super(message);
    }

    public int getErrorCode() {
        return 4;
    }
}

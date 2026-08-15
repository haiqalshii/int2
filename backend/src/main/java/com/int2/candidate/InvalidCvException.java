package com.int2.candidate;

public class InvalidCvException extends RuntimeException {
    public InvalidCvException(String message) { super(message); }
    public InvalidCvException(String message, Throwable cause) { super(message, cause); }
}


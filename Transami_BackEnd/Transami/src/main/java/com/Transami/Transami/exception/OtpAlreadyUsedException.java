package com.Transami.Transami.exception;

public class OtpAlreadyUsedException extends RuntimeException {
    public OtpAlreadyUsedException(String message) {
        super(message);
    }
}
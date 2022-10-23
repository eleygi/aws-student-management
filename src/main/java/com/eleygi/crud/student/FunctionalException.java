package com.eleygi.crud.student;

public class FunctionalException extends RuntimeException {
    private final int httpStatusCode;

    public FunctionalException(int httpStatusCode, String message) {
        super(message);
        this.httpStatusCode = httpStatusCode;
    }

    public int getHttpStatusCode() {
        return httpStatusCode;
    }
}

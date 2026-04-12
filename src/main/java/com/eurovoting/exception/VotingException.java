package com.eurovoting.exception;

import org.springframework.http.HttpStatus;

public class VotingException extends RuntimeException {

    private final HttpStatus status;

    public VotingException(String message) {
        super(message);
        this.status = HttpStatus.CONFLICT;
    }

    public VotingException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}

package com.sobolev.spring.taskmanagementsystem.exception;

public class AccessDeniedException extends RuntimeException {
    public AccessDeniedException() {
        super();
    }

    public AccessDeniedException(String message) {
        super(message);
    }
}

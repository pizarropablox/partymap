package com.partymap.backend.Exceptions;


public class NotFoundException extends RuntimeException {
    
    private static final long serialVersionUID = 1;

    public NotFoundException(String message) {
        super(message);
    }
}

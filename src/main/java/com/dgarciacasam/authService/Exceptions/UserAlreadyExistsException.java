package com.dgarciacasam.authService.Exceptions;

public class UserAlreadyExistsException  extends RuntimeException {
    public UserAlreadyExistsException (String message) {
        super(message); 
    }
}

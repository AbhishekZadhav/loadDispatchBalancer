package com.example.LoadBalancer.CustomExceptions;

import java.sql.SQLIntegrityConstraintViolationException;

public class DuplicateEntriesException extends SQLIntegrityConstraintViolationException {
    String message;
    public DuplicateEntriesException(String message){
        this.message = message;
    }
}

package com.example.LoadBalancer.Rest;

import com.example.LoadBalancer.CustomExceptions.DuplicateEntriesException;
import com.example.LoadBalancer.CustomExceptions.NotEnoughPackagesException;
import com.example.LoadBalancer.CustomExceptions.NotEnoughVehiclesException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import com.example.LoadBalancer.Responses.ErrorResponse;
@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(NotEnoughVehiclesException.class)
    public ResponseEntity<ErrorResponse> handleNotEnoughVehicleException(NotEnoughVehiclesException e){
        ErrorResponse error = new ErrorResponse(e.getMessage());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(NotEnoughPackagesException.class)
    public ResponseEntity<ErrorResponse> handleNotEnoughPackagesException(NotEnoughPackagesException e){
        ErrorResponse error = new ErrorResponse(e.getMessage());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(DuplicateEntriesException.class)
    public ResponseEntity<ErrorResponse> handleNotEnoughPackagesException(DuplicateEntriesException e){
        ErrorResponse error = new ErrorResponse(e.getMessage());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }
}

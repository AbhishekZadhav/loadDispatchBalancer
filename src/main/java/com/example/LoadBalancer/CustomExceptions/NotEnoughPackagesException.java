package com.example.LoadBalancer.CustomExceptions;

public class NotEnoughPackagesException extends Exception{
    String message;
    public NotEnoughPackagesException(String message){
        this.message = message;
    }
}

package com.example.LoadBalancer.CustomExceptions;

public class NotEnoughVehiclesException extends Exception{
    String message;
    public NotEnoughVehiclesException(String message){
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

package com.example.LoadBalancer.Entity.DTO;

import com.example.LoadBalancer.Entity.Vehicle;

import java.util.List;

public class VehicleRequest {
    private List<Vehicle> vehicles;

    public List<Vehicle> getVehicles() {
        return vehicles;
    }

    public void setVehicles(List<Vehicle> vehicles) {
        this.vehicles = vehicles;
    }
}

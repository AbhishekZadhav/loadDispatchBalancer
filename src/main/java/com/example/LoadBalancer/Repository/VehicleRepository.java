package com.example.LoadBalancer.Repository;

import com.example.LoadBalancer.Entity.Vehicle;

import java.util.List;

public interface VehicleRepository {
    void save(Vehicle theVehicle);
    List<Vehicle> findAll();
}

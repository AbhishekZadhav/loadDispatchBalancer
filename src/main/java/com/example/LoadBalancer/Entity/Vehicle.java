package com.example.LoadBalancer.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
//{
//        "vehicleId": "VEH001",
//        "capacity": 100,
//        "currentLatitude": 12.9716,
//        "currentLongitude": 77.6413,
//        "currentAddress": "Indiranagar, Bangalore, Karnataka, India"
//        }

@Entity
@Table(name="vehicles")
public class Vehicle {
    @Id
    @Column(name="vehicleId")
    private String vehicleId;
    @Column(name="capacity")
    private int capacity;
    @Column(name="currentLatitude")
    private double currentLatitude;
    @Column(name="currentLongitude")
    private double currentLongitude;
    @Column(name="currentAddress")
    private String currentAddress;
    public Vehicle(){}
    public Vehicle(String vehicleId, int capacity, double currentLatitude, double currentLongitude, String currentAddress) {
        this.vehicleId = vehicleId;
        this.capacity = capacity;
        this.currentLatitude = currentLatitude;
        this.currentLongitude = currentLongitude;
        this.currentAddress = currentAddress;
    }

    public String getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(String vehicleId) {
        this.vehicleId = vehicleId;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public double getCurrentLatitude() {
        return currentLatitude;
    }

    public void setCurrentLatitude(double currentLatitude) {
        this.currentLatitude = currentLatitude;
    }

    public double getCurrentLongitude() {
        return currentLongitude;
    }

    public void setCurrentLongitude(double currentLongitude) {
        this.currentLongitude = currentLongitude;
    }

    public String getCurrentAddress() {
        return currentAddress;
    }

    public void setCurrentAddress(String currentAddress) {
        this.currentAddress = currentAddress;
    }
}

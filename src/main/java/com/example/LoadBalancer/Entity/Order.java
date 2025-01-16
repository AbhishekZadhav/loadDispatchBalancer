package com.example.LoadBalancer.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

//{
//        "orderId": "ORD001",
//        "latitude": 12.9716,
//        "longitude": 77.5946,
//        "address": "MG Road, Bangalore, Karnataka, India",
//        "packageWeight": 10,
//        "priority": "HIGH"
//        }
@Entity
@Table(name="orders")
public class Order {
    @Id
    @Column(name="orderId")
    private String orderId;

    @Column(name="latitude")
    private double latitude;

    @Column(name="longitude")
    private double longitude;

    @Column(name="address")
    private String address;

    @Column(name="packageWeight")
    private int packageWeight;

    @Column(name="priority")
    private String priority;
    public Order(){}
    public Order(String orderId, double latitude, double longitude, String address, int packageWeight, String priority) {
        this.orderId = orderId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;
        this.packageWeight = packageWeight;
        this.priority = priority;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getPackageWeight() {
        return packageWeight;
    }

    public void setPackageWeight(int packageWeight) {
        this.packageWeight = packageWeight;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }
}

package com.example.LoadBalancer.Responses;

//{
//        "vehicleId": "VEH002",
//        "totalLoad": 20,
//        "totalDistance": "6 km",
//        "assignedOrders": [
//        {
//        "orderId": "ORD002",
//        "latitude": 13.0827,
//        "longitude": 80.2707,
//        "address": "Anna Salai, Chennai, Tamil Nadu, India",
//        "packageWeight": 20,
//        "priority": "MEDIUM"
//        }
//        ]
//        }

import com.example.LoadBalancer.Entity.Order;

import java.util.List;

public class VehicleAssignment {
    private String vehicleId;
    private int totalLoad;
    private String totalDistance;
    private List<Order> assignedOrders;

    public VehicleAssignment(String vehicleId, int totalLoad, String totalDistance, List<Order> assignedOrders) {
        this.vehicleId = vehicleId;
        this.totalLoad = totalLoad;
        this.totalDistance = totalDistance;
        this.assignedOrders = assignedOrders;
    }

    public String getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(String vehicleId) {
        this.vehicleId = vehicleId;
    }

    public int getTotalLoad() {
        return totalLoad;
    }

    public void setTotalLoad(int totalLoad) {
        this.totalLoad = totalLoad;
    }

    public String getTotalDistance() {
        return totalDistance;
    }

    public void setTotalDistance(String totalDistance) {
        this.totalDistance = totalDistance;
    }

    public List<Order> getAssignedOrders() {
        return assignedOrders;
    }

    public void setAssignedOrders(List<Order> assignedOrders) {
        this.assignedOrders = assignedOrders;
    }
}

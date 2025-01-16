package com.example.LoadBalancer.Beans;

import com.example.LoadBalancer.CustomExceptions.NotEnoughVehiclesException;
import com.example.LoadBalancer.Entity.Order;
import com.example.LoadBalancer.Entity.Vehicle;
import com.example.LoadBalancer.Responses.DispatchPlan;
import com.example.LoadBalancer.Responses.VehicleAssignment;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

class VehicleExt extends Vehicle {
    double distance;

    public VehicleExt(Vehicle theVehicle, double distance) {
        super(theVehicle.getVehicleId(), theVehicle.getCapacity(), theVehicle.getCurrentLatitude(),
                theVehicle.getCurrentLongitude(), theVehicle.getCurrentAddress());
        this.distance = distance;
    }

    public Vehicle getParent() {
        return this;
    }
}

class OrderExt extends Order {
    int priority;
    double distance;

    public OrderExt(Order theOrder) {
        super(theOrder.getOrderId(), theOrder.getLatitude(), theOrder.getLongitude(),
                theOrder.getAddress(), theOrder.getPackageWeight(), theOrder.getPriority());
        this.priority = convertPriorityToInt(theOrder.getPriority());
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public int getPriorityAsInt() {
        return this.priority;
    }

    @JsonIgnore
    public Order getParent() {
        return this;
    }

    private int convertPriorityToInt(String priority) {
        return switch (priority.toUpperCase()) {
            case "HIGH" -> 3;
            case "MEDIUM" -> 2;
            case "LOW" -> 1;
            default -> Integer.MAX_VALUE;
        };
    }
}

@Component
public class Distributor {
    private static final double EARTH_RADIUS = 6371;
    private final List<Order> orderList;
    private final List<Vehicle> vehicleList;

    public Distributor(List<Order> orderList, List<Vehicle> vehicleList) {
        this.orderList = orderList;
        this.vehicleList = vehicleList;
    }

    public DispatchPlan getDistribution() throws NotEnoughVehiclesException {
        // Convert and prioritize orders
        List<OrderExt> prioritizedOrders = orderList.stream()
                .map(OrderExt::new)
                .sorted(Comparator.comparingInt(OrderExt::getPriorityAsInt).reversed())
                .collect(Collectors.toList());

        // Prepare vehicle availability and assignment maps
        Map<String, Integer> vehicleCapacityMap = vehicleList.stream()
                .collect(Collectors.toMap(Vehicle::getVehicleId, Vehicle::getCapacity));

        Map<String, PriorityQueue<OrderExt>> vehicleAssignments = vehicleList.stream()
                .collect(Collectors.toMap(Vehicle::getVehicleId, v -> new PriorityQueue<>(Comparator.comparingDouble(o -> o.distance))));

        Map<String, Vehicle> vehiclePool = vehicleList.stream()
                .collect(Collectors.toMap(Vehicle::getVehicleId, v -> v));

        // Assign orders to vehicles
        for (OrderExt order : prioritizedOrders) {
            PriorityQueue<VehicleExt> vehicleQueue = getVehiclesSortedByDistance(order);

            boolean isAssigned = false;
            while (!vehicleQueue.isEmpty()) {
                VehicleExt vehicle = vehicleQueue.poll();
                if (vehicleCapacityMap.get(vehicle.getVehicleId()) >= order.getPackageWeight()) {
                    vehicleCapacityMap.put(vehicle.getVehicleId(),
                            vehicleCapacityMap.get(vehicle.getVehicleId()) - order.getPackageWeight());

                    order.setDistance(vehicle.distance);
                    vehicleAssignments.get(vehicle.getVehicleId()).add(order);
                    isAssigned = true;
                    break;
                }
            }

            if (!isAssigned) {
                throw new NotEnoughVehiclesException("Not enough vehicles available for order " + order.getOrderId());
            }
        }

        // Create dispatch plan
        return createDispatchPlan(vehicleAssignments, vehicleCapacityMap, vehiclePool);
    }

    private PriorityQueue<VehicleExt> getVehiclesSortedByDistance(OrderExt order) {
        return vehicleList.stream()
                .map(vehicle -> new VehicleExt(vehicle, calculateHaversineDistance(
                        vehicle.getCurrentLatitude(),
                        vehicle.getCurrentLongitude(),
                        order.getLatitude(),
                        order.getLongitude())))
                .collect(Collectors.toCollection(() -> new PriorityQueue<>(Comparator.comparingDouble(v -> v.distance))));
    }

    private DispatchPlan createDispatchPlan(Map<String, PriorityQueue<OrderExt>> vehicleAssignments,
                                            Map<String, Integer> vehicleCapacityMap,
                                            Map<String, Vehicle> vehiclePool) {
        DispatchPlan plan = new DispatchPlan();

        vehicleAssignments.forEach((vehicleId, orders) -> {
            Vehicle vehicle = vehiclePool.get(vehicleId);
            List<Order> orderList = new ArrayList<>();
            double totalDistance = calculateTotalDistance(vehicle, orders, orderList);
            int usedCapacity = vehicle.getCapacity() - vehicleCapacityMap.get(vehicleId);

            VehicleAssignment assignment = new VehicleAssignment(vehicleId, usedCapacity, totalDistance + " Km", orderList);
            plan.add(assignment);
        });

        return plan;
    }

    private double calculateTotalDistance(Vehicle vehicle, PriorityQueue<OrderExt> orders, List<Order> orderList) {
        double prevLat = vehicle.getCurrentLatitude();
        double prevLon = vehicle.getCurrentLongitude();
        double totalDistance = 0;

        while (!orders.isEmpty()) {
            OrderExt order = orders.poll();
            totalDistance += calculateHaversineDistance(prevLat, prevLon, order.getLatitude(), order.getLongitude());
            prevLat = order.getLatitude();
            prevLon = order.getLongitude();
            orderList.add(order);
        }

        totalDistance += calculateHaversineDistance(prevLat, prevLon, vehicle.getCurrentLatitude(), vehicle.getCurrentLongitude());
        return totalDistance;
    }

    private double calculateHaversineDistance(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(lat1) * Math.cos(lat2) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);

        return EARTH_RADIUS * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }
}
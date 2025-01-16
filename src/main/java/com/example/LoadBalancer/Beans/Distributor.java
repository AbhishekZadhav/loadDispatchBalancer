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
    final double distance;

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
    final int priority;
    private double distance;

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

    private static int convertPriorityToInt(String priority) {
        if (priority == null) {
            return Integer.MIN_VALUE;
        }
        return switch (priority.toUpperCase()) {
            case "HIGH" -> 3;
            case "MEDIUM" -> 2;
            case "LOW" -> 1;
            default -> Integer.MIN_VALUE;
        };
    }
}

class DeliveryPoint {
    final double latitude;
    final double longitude;
    final OrderExt order;

    DeliveryPoint(double latitude, double longitude, OrderExt order) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.order = order;
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
        final List<OrderExt> prioritizedOrders = orderList.stream()
                .map(OrderExt::new)
                .sorted(Comparator.comparingInt(OrderExt::getPriorityAsInt).reversed()
                        .thenComparingDouble(Order::getPackageWeight).reversed())
                .collect(Collectors.toList());

        // Initialize vehicle tracking
        final Map<String, Integer> remainingCapacity = new HashMap<>();
        final Map<String, List<OrderExt>> vehicleAssignments = new HashMap<>();

        for (Vehicle vehicle : vehicleList) {
            remainingCapacity.put(vehicle.getVehicleId(), vehicle.getCapacity());
            vehicleAssignments.put(vehicle.getVehicleId(), new ArrayList<>());
        }

        // Assign orders to vehicles
        for (OrderExt order : prioritizedOrders) {
            if (!assignOrder(order, vehicleAssignments, remainingCapacity)) {
                throw new NotEnoughVehiclesException("Not enough vehicles available for order " + order.getOrderId());
            }
        }

        // Create final dispatch plan
        return createDispatchPlan(vehicleAssignments, remainingCapacity);
    }

    private boolean assignOrder(OrderExt order,
                                Map<String, List<OrderExt>> vehicleAssignments,
                                Map<String, Integer> remainingCapacity) {
        // First pass: Try nearest vehicle with sufficient capacity
        final List<VehicleExt> sortedVehicles = getSortedVehiclesByDistance(order);

        for (VehicleExt vehicle : sortedVehicles) {
            final int currentCapacity = remainingCapacity.get(vehicle.getVehicleId());
            if (currentCapacity >= order.getPackageWeight()) {
                assignOrderToVehicle(order, vehicle, currentCapacity, vehicleAssignments, remainingCapacity);
                return true;
            }
        }

        // Second pass: Try any vehicle with sufficient capacity
        for (Vehicle vehicle : vehicleList) {
            final int currentCapacity = remainingCapacity.get(vehicle.getVehicleId());
            if (currentCapacity >= order.getPackageWeight()) {
                final double distance = calculateHaversineDistance(
                        vehicle.getCurrentLatitude(),
                        vehicle.getCurrentLongitude(),
                        order.getLatitude(),
                        order.getLongitude()
                );
                final VehicleExt vehicleExt = new VehicleExt(vehicle, distance);
                assignOrderToVehicle(order, vehicleExt, currentCapacity, vehicleAssignments, remainingCapacity);
                return true;
            }
        }

        return false;
    }

    private void assignOrderToVehicle(OrderExt order,
                                      VehicleExt vehicle,
                                      int currentCapacity,
                                      Map<String, List<OrderExt>> vehicleAssignments,
                                      Map<String, Integer> remainingCapacity) {
        order.setDistance(vehicle.distance);
        remainingCapacity.put(vehicle.getVehicleId(), currentCapacity - order.getPackageWeight());
        vehicleAssignments.get(vehicle.getVehicleId()).add(order);
    }

    private List<VehicleExt> getSortedVehiclesByDistance(final OrderExt order) {
        return vehicleList.stream()
                .map(vehicle -> new VehicleExt(vehicle, calculateHaversineDistance(
                        vehicle.getCurrentLatitude(),
                        vehicle.getCurrentLongitude(),
                        order.getLatitude(),
                        order.getLongitude())))
                .sorted(Comparator.comparingDouble(v -> v.distance))
                .collect(Collectors.toList());
    }

    private DispatchPlan createDispatchPlan(final Map<String, List<OrderExt>> vehicleAssignments,
                                            final Map<String, Integer> remainingCapacity) {
        final DispatchPlan plan = new DispatchPlan();

        vehicleAssignments.forEach((vehicleId, orders) -> {
            if (!orders.isEmpty()) {
                final Vehicle vehicle = vehicleList.stream()
                        .filter(v -> v.getVehicleId().equals(vehicleId))
                        .findFirst()
                        .orElseThrow();

                final double totalDistance = calculateTotalDistance(vehicle, new ArrayList<>(orders));
                final int usedCapacity = vehicle.getCapacity() - remainingCapacity.get(vehicleId);

                final List<Order> originalOrders = orders.stream()
                        .map(OrderExt::getParent)
                        .collect(Collectors.toList());

                final VehicleAssignment assignment = new VehicleAssignment(vehicleId, usedCapacity,
                        String.format("%.2f Km", totalDistance), originalOrders);
                plan.add(assignment);
            }
        });

        return plan;
    }

    private double calculateTotalDistance(final Vehicle vehicle, final List<OrderExt> orders) {
        if (orders.isEmpty()) {
            return 0.0;
        }

        // Create starting point
        final DeliveryPoint startPoint = new DeliveryPoint(
                vehicle.getCurrentLatitude(),
                vehicle.getCurrentLongitude(),
                null
        );

        // Convert orders to delivery points
        final List<DeliveryPoint> deliveryPoints = new ArrayList<>();
        for (OrderExt order : orders) {
            deliveryPoints.add(new DeliveryPoint(order.getLatitude(), order.getLongitude(), order));
        }

        // Sort delivery points by distance from start point
        final DeliveryPoint finalStartPoint = startPoint;  // Need final reference for lambda
        deliveryPoints.sort((p1, p2) -> Double.compare(
                calculateHaversineDistance(finalStartPoint.latitude, finalStartPoint.longitude, p1.latitude, p1.longitude),
                calculateHaversineDistance(finalStartPoint.latitude, finalStartPoint.longitude, p2.latitude, p2.longitude)
        ));

        // Calculate total route distance
        double totalDistance = 0.0;
        DeliveryPoint currentPoint = startPoint;

        for (DeliveryPoint nextPoint : deliveryPoints) {
            totalDistance += calculateHaversineDistance(
                    currentPoint.latitude, currentPoint.longitude,
                    nextPoint.latitude, nextPoint.longitude
            );
            currentPoint = nextPoint;
        }

        // Add return distance to start point
        totalDistance += calculateHaversineDistance(
                currentPoint.latitude, currentPoint.longitude,
                startPoint.latitude, startPoint.longitude
        );

        return totalDistance;
    }

    private double calculateHaversineDistance(final double lat1, final double lon1,
                                              final double lat2, final double lon2) {
        final double dLat = Math.toRadians(lat2 - lat1);
        final double dLon = Math.toRadians(lon2 - lon1);
        final double lat1Rad = Math.toRadians(lat1);
        final double lat2Rad = Math.toRadians(lat2);

        final double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);

        return EARTH_RADIUS * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }
}
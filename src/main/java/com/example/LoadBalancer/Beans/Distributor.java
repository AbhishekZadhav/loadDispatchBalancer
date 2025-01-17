package com.example.LoadBalancer.Beans;

import com.example.LoadBalancer.CustomExceptions.NotEnoughVehiclesException;
import com.example.LoadBalancer.Entity.Order;
import com.example.LoadBalancer.Entity.Vehicle;
import com.example.LoadBalancer.Responses.DispatchPlan;
import com.example.LoadBalancer.Responses.VehicleAssignment;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.stereotype.Component;

import java.util.*;

class VehicleExt extends Vehicle{
    double distance;

    public VehicleExt(Vehicle theVehicle, double distance) {
        super(theVehicle.getVehicleId(), theVehicle.getCapacity(), theVehicle.getCurrentLatitude(),
                theVehicle.getCurrentLongitude(), theVehicle.getCurrentAddress());
        this.distance = distance;
    }
    public Vehicle getParent(){
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

    public void setDistance(Double distance){
        this.distance = distance;
    }
    public int getPriorityAsInt() {
        return this.priority;
    }
    @JsonIgnore
    public Order getParent(){
        return this;
    }
    public void setPriority(int priority) {
        this.priority = priority;
    }

    private int convertPriorityToInt(String priority) {
        if ("HIGH".equalsIgnoreCase(priority)) {
            return 3;
        } else if ("MEDIUM".equalsIgnoreCase(priority)) {
            return 2;
        } else if ("LOW".equalsIgnoreCase(priority)) {
            return 1;
        }
        return Integer.MAX_VALUE;
    }
}
@Component
public class Distributor {
    List<Order> orderList;
    List<Vehicle> vehicleList;
    private static final double EARTH_RADIUS = 6371;
    public Distributor(List<Order> orderList, List<Vehicle> vehicleList) {
        this.orderList = orderList;
        this.vehicleList = vehicleList;
    }
    public DispatchPlan getDistribution() throws NotEnoughVehiclesException {
        List<OrderExt> orderWithPriority = new ArrayList<>();
        int totalOrderWeight = 0;
        for(int i=0; i<orderList.size(); i++){
            totalOrderWeight+=orderList.get(i).getPackageWeight();
            orderWithPriority.add(new OrderExt(orderList.get(i)));
        }
        Collections.sort(orderWithPriority, (o1, o2) -> Integer.compare(o2.priority, o1.priority));
        Map<OrderExt,PriorityQueue<VehicleExt>> sortedMap = new LinkedHashMap<>();
        for(int i=0; i<orderWithPriority.size(); i++){
            OrderExt theOrder = orderWithPriority.get(i);
            PriorityQueue<VehicleExt> pq = new PriorityQueue<>((v1, v2)->Double.compare(v1.distance, v2.distance));
            for(int j=0; j<vehicleList.size(); j++){
                Vehicle theVehicle = vehicleList.get(j);
                double vehicleDistance = calculateHaversineDistance(theVehicle.getCurrentLatitude(),
                        theVehicle.getCurrentLongitude(), theOrder.getLatitude(), theOrder.getLongitude());
                pq.add(new VehicleExt(theVehicle, vehicleDistance));
            }
            sortedMap.put(theOrder, pq);
        }
        HashMap<String, Integer> vehicleHash = new HashMap<>();
        HashMap<String, Vehicle> vehiclePool = new HashMap<>();
        HashMap<String, PriorityQueue<OrderExt>> assignedHash = new HashMap<>();
        int totalVehicleCapacity = 0;
        for(int j=0; j<vehicleList.size(); j++){
            Vehicle theVehicle = vehicleList.get(j);
            totalVehicleCapacity+=theVehicle.getCapacity();
            vehicleHash.put(theVehicle.getVehicleId(), theVehicle.getCapacity());
            PriorityQueue<OrderExt> pq = new PriorityQueue<>((o1, o2)->Double.compare(o1.distance, o2.distance));
            assignedHash.put(theVehicle.getVehicleId(), pq);
            vehiclePool.put(theVehicle.getVehicleId(), theVehicle);
        }
        if(totalVehicleCapacity<totalOrderWeight){
            throw new NotEnoughVehiclesException("Sorry we don't have enough vehicles yet");
        }
        for(Map.Entry<OrderExt, PriorityQueue<VehicleExt>> entry : sortedMap.entrySet()){
            OrderExt order = entry.getKey();
            PriorityQueue<VehicleExt> vehicleQueue = entry.getValue();
            boolean isAssigned = false;
            while(!vehicleQueue.isEmpty()){
                VehicleExt theVehicle = vehicleQueue.poll();
                if(vehicleHash.get(theVehicle.getVehicleId())>=order.getPackageWeight()){
                    vehicleHash.put(theVehicle.getVehicleId(),
                            vehicleHash.get(theVehicle.getVehicleId())-order.getPackageWeight());
                    order.setDistance(theVehicle.distance);
                    PriorityQueue<OrderExt> pq = assignedHash.get(theVehicle.getVehicleId());
                    pq.add(order);
                    assignedHash.put(theVehicle.getVehicleId(), pq);
                    isAssigned = true;
                    break;
                }
            }
            if(!isAssigned){
                throw new NotEnoughVehiclesException("Sorry we don't have enough vehicles yet");
            }

        }
        DispatchPlan plan = new DispatchPlan();
        for(String vehicleID:assignedHash.keySet()){
            Vehicle theVehicle = vehiclePool.get(vehicleID);
            PriorityQueue<OrderExt> pq = assignedHash.get(theVehicle.getVehicleId());
            List<Order> orderList = new ArrayList<>();
            String totalDistance = getTotalDistance(theVehicle, orderList, pq)+" Km";
            int totalCapacity = theVehicle.getCapacity()-vehicleHash.get(theVehicle.getVehicleId());
            VehicleAssignment assignedVehicle = new VehicleAssignment(theVehicle.getVehicleId(),
                    totalCapacity, totalDistance, orderList);
            plan.add(assignedVehicle);
        }
        return plan;

    }
    private double getTotalDistance(Vehicle theVehicle, List<Order> orderList, PriorityQueue<OrderExt> pq) {
        double prevlat1 = theVehicle.getCurrentLatitude();
        double prevlon1 = theVehicle.getCurrentLongitude();
        double totalDistance = 0;
        while(!pq.isEmpty()) {
            OrderExt theOrder = pq.poll();
            totalDistance += calculateHaversineDistance(prevlat1, prevlon1, theOrder.getLatitude(), theOrder.getLongitude());
            prevlon1 = theOrder.getLongitude();
            prevlat1 = theOrder.getLatitude();
            orderList.add(theOrder);
        }
        totalDistance += calculateHaversineDistance(prevlat1, prevlon1,
                theVehicle.getCurrentLatitude(), theVehicle.getCurrentLongitude());
        return totalDistance;
    }
    private double calculateHaversineDistance(double lat1, double lon1, double lat2, double lon2) {
        // Convert latitude and longitude to radians
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);

        // Haversine formula
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(lat1) * Math.cos(lat2) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS * c;
    }
}

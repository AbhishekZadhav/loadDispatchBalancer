package com.example.LoadBalancer.Responses;

import java.util.ArrayList;
import java.util.List;

public class DispatchPlan {
    List<VehicleAssignment> dispatchPlan;

    public DispatchPlan() {
        this.dispatchPlan = new ArrayList<>();
    }
    public void add(VehicleAssignment theVehicle){
        dispatchPlan.add(theVehicle);
    }

    public List<VehicleAssignment> getDispatchPlan() {
        return dispatchPlan;
    }

    public void setDispatchPlan(List<VehicleAssignment> dispatchPlan) {
        this.dispatchPlan = dispatchPlan;
    }
}

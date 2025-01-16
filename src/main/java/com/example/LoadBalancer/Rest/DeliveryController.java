package com.example.LoadBalancer.Rest;

import com.example.LoadBalancer.CustomExceptions.DuplicateEntriesException;
import com.example.LoadBalancer.CustomExceptions.NotEnoughPackagesException;
import com.example.LoadBalancer.CustomExceptions.NotEnoughVehiclesException;
import com.example.LoadBalancer.Entity.DTO.OrderRequest;
import com.example.LoadBalancer.Entity.DTO.VehicleRequest;
import com.example.LoadBalancer.Entity.Order;
import com.example.LoadBalancer.Entity.Vehicle;
import com.example.LoadBalancer.Responses.DispatchPlan;
import com.example.LoadBalancer.Responses.NewEntityResponse;
import com.example.LoadBalancer.Services.DeliveryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dispatch")
public class DeliveryController {
    private DeliveryService service;

    @Autowired
    public DeliveryController(DeliveryService service) {
        this.service = service;
    }

    @PostMapping("/orders")
    public NewEntityResponse createNewOrders(@RequestBody OrderRequest theRequest) throws DuplicateEntriesException {
        return service.createNewOrder(theRequest);
    }

    @PostMapping("/vehicles")
    public NewEntityResponse createNewVehicles(@RequestBody VehicleRequest theRequest) throws DuplicateEntriesException {
        return service.createNewVehicle(theRequest);
    }
    @GetMapping("/plan")
    public DispatchPlan getPlan() throws NotEnoughVehiclesException, NotEnoughPackagesException {
        return service.getPlan();
    }
}

package com.example.LoadBalancer.Services;

import com.example.LoadBalancer.Beans.Distributor;
import com.example.LoadBalancer.CustomExceptions.DuplicateEntriesException;
import com.example.LoadBalancer.CustomExceptions.NotEnoughPackagesException;
import com.example.LoadBalancer.CustomExceptions.NotEnoughVehiclesException;
import com.example.LoadBalancer.Entity.DTO.OrderRequest;
import com.example.LoadBalancer.Entity.DTO.VehicleRequest;
import com.example.LoadBalancer.Entity.Order;
import com.example.LoadBalancer.Entity.Vehicle;
import com.example.LoadBalancer.Repository.OrderRepository;
import com.example.LoadBalancer.Repository.VehicleRepository;
import com.example.LoadBalancer.Responses.DispatchPlan;
import com.example.LoadBalancer.Responses.NewEntityResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;


@Service
public class DeliveryService {
    private OrderRepository orderRepo;
    private VehicleRepository vehicleRepo;

    @Autowired
    public DeliveryService(OrderRepository orderRepo, VehicleRepository vehicleRepo) {
        this.orderRepo = orderRepo;
        this.vehicleRepo = vehicleRepo;
    }
    @Transactional
    public NewEntityResponse createNewOrder (OrderRequest theRequest)throws DuplicateEntriesException {
        List<Order> request = theRequest.getOrders();
        for(Order theOrder:request) {
            orderRepo.save(theOrder);

        }
        return new NewEntityResponse("Delivery orders accepted.", "success");
    }
    @Transactional
    public NewEntityResponse createNewVehicle(VehicleRequest theRequest) throws DuplicateEntriesException{
        List<Vehicle> request = theRequest.getVehicles();
        for(Vehicle theVehicle:request) {

            vehicleRepo.save(theVehicle);
        }
        return new NewEntityResponse("Vehicle details accepted.", "success");
    }

    private List<Vehicle> findAllVehicles(){
        return vehicleRepo.findAll();
    }
    private List<Order> findAllOrders(){
        return orderRepo.findAll();
    }

    public DispatchPlan getPlan() throws NotEnoughVehiclesException, NotEnoughPackagesException {
        List<Vehicle> vehicleList = findAllVehicles();
        List<Order> orderList = findAllOrders();
        if(vehicleList.isEmpty()){
            throw new NotEnoughVehiclesException("Sorry we don't have enough vehicles yet");
        }
        if(orderList.isEmpty()){
            throw new NotEnoughPackagesException("Sorry we don't have packages to deliver");
        }
        Distributor theDistributor = new Distributor(orderList, vehicleList);
        return theDistributor.getDistribution();
    }

}

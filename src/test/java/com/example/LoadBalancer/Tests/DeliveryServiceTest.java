package com.example.LoadBalancer.Tests;
import static org.mockito.Mockito.doNothing;
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
import com.example.LoadBalancer.Services.DeliveryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DeliveryServiceTest {

    @Mock
    private OrderRepository orderRepo;

    @Mock
    private VehicleRepository vehicleRepo;

    @InjectMocks
    private DeliveryService deliveryService;

    private List<Order> orders;
    private List<Vehicle> vehicles;
    private OrderRequest orderRequest;
    private VehicleRequest vehicleRequest;

    @BeforeEach
    void setUp() {
        orders = new ArrayList<>();
        vehicles = new ArrayList<>();

        // Setup some sample data
        Order order1 = new Order();
        order1.setOrderId(String.valueOf(1));
        orders.add(order1);

        Vehicle vehicle1 = new Vehicle();
        vehicle1.setVehicleId(String.valueOf(1));
        vehicles.add(vehicle1);

        orderRequest = new OrderRequest();
        orderRequest.setOrders(orders);

        vehicleRequest = new VehicleRequest();
        vehicleRequest.setVehicles(vehicles);
    }


    @Test
    void testCreateNewOrder_Success() throws DuplicateEntriesException {
        // Mock the behavior of orderRepo.save() method
        doNothing().when(orderRepo).save(any(Order.class));  // Mocking void return method

        // Call the service method
        NewEntityResponse response = deliveryService.createNewOrder(orderRequest);

        // Assert the result
        assertEquals("Delivery orders accepted.", response.getMessage());
        assertEquals("success", response.getStatus());

        // Verify save was called
        verify(orderRepo, times(1)).save(any(Order.class));
    }

    @Test
    void testCreateNewVehicle_Success() throws DuplicateEntriesException {
        // Mock the behavior of vehicleRepo.save() method (since it's a void method)
        doNothing().when(vehicleRepo).save(any(Vehicle.class));  // Mocking void return method

        // Call the service method
        NewEntityResponse response = deliveryService.createNewVehicle(vehicleRequest);

        // Assert the result
        assertEquals("Vehicle details accepted.", response.getMessage());
        assertEquals("success", response.getStatus());

        // Verify save was called
        verify(vehicleRepo, times(1)).save(any(Vehicle.class));
    }

    @Test
    void testGetPlan_NoVehicles() {
        // Mock an empty vehicle list
        when(vehicleRepo.findAll()).thenReturn(new ArrayList<>());

        // Call the service method and check for exception
        NotEnoughVehiclesException exception = assertThrows(NotEnoughVehiclesException.class,
                () -> deliveryService.getPlan());

        assertEquals("Sorry we don't have enough vehicles yet", exception.getMessage());
    }


    @Test
    void testGetPlan_Success() throws NotEnoughVehiclesException, NotEnoughPackagesException {
        // Mock the data
        when(vehicleRepo.findAll()).thenReturn(vehicles);
        when(orderRepo.findAll()).thenReturn(orders);

        // Call the service method
        DispatchPlan plan = deliveryService.getPlan();

        // Check if the plan is returned correctly
        assertNotNull(plan);  // Assuming DispatchPlan is non-null in this case
    }
}

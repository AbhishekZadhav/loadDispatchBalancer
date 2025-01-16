package com.example.LoadBalancer.Repository;

import com.example.LoadBalancer.Entity.Order;
import com.example.LoadBalancer.CustomExceptions.DuplicateEntriesException;
import java.util.List;

public interface OrderRepository {
    void save (Order theOrder);
    List<Order> findAll();
}

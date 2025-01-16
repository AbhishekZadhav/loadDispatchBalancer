package com.example.LoadBalancer.Entity.DTO;

import com.example.LoadBalancer.Entity.Order;

import java.util.List;

public class OrderRequest {
    private List<Order> orders;

    public List<Order> getOrders() {
        return orders;
    }

    public void setOrders(List<Order> orders) {
        this.orders = orders;
    }
}

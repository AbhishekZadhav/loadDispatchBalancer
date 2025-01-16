package com.example.LoadBalancer.Repository;

import com.example.LoadBalancer.Entity.Order;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import com.example.LoadBalancer.CustomExceptions.DuplicateEntriesException;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;

@Repository
public class OrderRepositoryImpl implements OrderRepository{
    private EntityManager entityManager;
    @Autowired
    public OrderRepositoryImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }
    @Override
    public void save (Order theOrder){
        entityManager.persist(theOrder);
    }

    @Override
    public List<Order> findAll() {
        String jpql = "SELECT o FROM Order o";
        TypedQuery<Order> query = entityManager.createQuery(jpql, Order.class);
        return query.getResultList();
    }
}

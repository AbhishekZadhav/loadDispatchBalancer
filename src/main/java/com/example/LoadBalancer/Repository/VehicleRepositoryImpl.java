package com.example.LoadBalancer.Repository;

import com.example.LoadBalancer.Entity.Vehicle;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class VehicleRepositoryImpl implements VehicleRepository{

    private EntityManager entityManager;
    @Autowired
    public VehicleRepositoryImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }
    @Override
    public void save(Vehicle theVehicle) {
        entityManager.persist(theVehicle);
    }

    @Override
    public List<Vehicle> findAll() {
        String jpql = "SELECT v FROM Vehicle v";
        TypedQuery<Vehicle> query = entityManager.createQuery(jpql, Vehicle.class);
        return query.getResultList();
    }
}

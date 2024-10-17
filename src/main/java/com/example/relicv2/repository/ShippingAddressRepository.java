package com.example.relicv2.repository;

import com.example.relicv2.model.ShippingAddress;
import com.example.relicv2.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ShippingAddressRepository extends JpaRepository<ShippingAddress, Long> {
    Optional<ShippingAddress> findByUser(User user);
}

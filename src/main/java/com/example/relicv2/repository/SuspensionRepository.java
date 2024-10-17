package com.example.relicv2.repository;

import com.example.relicv2.model.Suspension;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SuspensionRepository extends JpaRepository<Suspension, Long> {
    List<Suspension> findByUserId(Long userId);
}

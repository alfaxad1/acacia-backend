package com.example.acacia.repository;

import com.example.acacia.model.FineType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FineTypeRepository extends JpaRepository<FineType, Long> {
    Optional<FineType> findByNameIgnoreCase(String name);
}

package com.example.acacia.repository;

import com.example.acacia.model.B2cTransferRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface B2cTransferRequestRepository extends JpaRepository<B2cTransferRequest, Long> {
}

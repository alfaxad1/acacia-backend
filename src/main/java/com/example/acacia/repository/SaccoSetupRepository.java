package com.example.acacia.repository;

import com.example.acacia.enums.SetupStatus;
import com.example.acacia.model.SaccoSetups;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SaccoSetupRepository extends JpaRepository<SaccoSetups,Long> {
    SaccoSetups findByStatus(SetupStatus setupStatus);
}

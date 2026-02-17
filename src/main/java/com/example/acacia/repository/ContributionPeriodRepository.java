package com.example.acacia.repository;

import com.example.acacia.model.ContributionPeriod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface ContributionPeriodRepository extends JpaRepository<ContributionPeriod,Long> {
    ContributionPeriod findByDate(LocalDate localDate);
}

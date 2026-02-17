package com.example.acacia.repository;

import com.example.acacia.model.ContributionArrear;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContributionArrearRepository extends JpaRepository<ContributionArrear,Long> {
}

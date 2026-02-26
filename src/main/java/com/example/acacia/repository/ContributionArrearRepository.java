package com.example.acacia.repository;

import com.example.acacia.model.ContributionArrear;
import jakarta.persistence.Tuple;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContributionArrearRepository extends JpaRepository<ContributionArrear,Long> {
    @Query("select a.id, a.amount, m.fullName, p.date, f.amount from ContributionArrear a join a.member m join a.fine f join a.period p")
    Page<Tuple> findArrears(Pageable pageable);
}

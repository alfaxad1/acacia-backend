package com.example.acacia.repository;

import com.example.acacia.enums.FineStatus;
import com.example.acacia.model.Fine;
import com.example.acacia.model.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FineRepository extends JpaRepository<Fine, Long> {
    List<Fine> findByMemberAndStatusOrderByIdAsc(Member member, FineStatus fineStatus);

    @Query("select count(f) from Fine f where f.member = :member and f.status = :fineStatus")
    int countUnpaid(Member member, FineStatus fineStatus);
}

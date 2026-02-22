package com.example.acacia.repository;

import com.example.acacia.enums.FineStatus;
import com.example.acacia.model.Fine;
import com.example.acacia.model.Member;
import jakarta.persistence.Tuple;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FineRepository extends JpaRepository<Fine, Long> {
    List<Fine> findByMemberAndStatusOrderByIdAsc(Member member, FineStatus fineStatus);

    @Query("select count(f) from Fine f where f.member = :member and f.status = :fineStatus")
    int countUnpaid(Member member, FineStatus fineStatus);

    @Query("select f.id, f.member.fullName, f.amount, f.fineDate, " +
            "f.status, f.type, f.paidDate " +
            "from Fine f " +
            "where f.status = :status")
    List<Tuple> findFines(FineStatus status);
}

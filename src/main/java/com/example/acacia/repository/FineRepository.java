package com.example.acacia.repository;

import com.example.acacia.enums.FineStatus;
import com.example.acacia.enums.FineTyp;
import com.example.acacia.model.Fine;
import com.example.acacia.model.Member;
import jakarta.persistence.Tuple;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface FineRepository extends JpaRepository<Fine, Long> {
    List<Fine> findByMemberAndStatusOrderByIdAsc(Member member, FineStatus fineStatus);

    @Query("select count(f) from Fine f where f.member = :member and f.status = :fineStatus")
    int countUnpaid(Member member, FineStatus fineStatus);

    @Query("select f.id, m.fullName, f.amount, f.fineDate, " +
            "f.status, f.type, f.paidDate,m.id " +
            "from Fine f join f.member m " +
            "where f.status = :status")
    List<Tuple> findFines(FineStatus status);

    @Query("select coalesce(sum(f.amount), 0) from Fine f where f.status = :fineStatus")
    BigDecimal sumTotalPaidFines(FineStatus fineStatus);

    @Query("select count(f), coalesce(sum(f.amount), 0) from Fine f join f.member m where m.id = :userId and f.status = :status")
    List<Object[]> getMemberFines(Long userId, FineStatus status);

    boolean existsByMemberAndTypeAndReferenceId(Member member, FineTyp type, Long referenceId);

    List<Fine> findByMemberAndStatusAndFineDateBeforeOrderByFineDateAsc(
            Member member,
            FineStatus status,
            LocalDate date);

    List<Fine> findByMemberAndStatusAndFineDateBefore(Member member, FineStatus fineStatus, LocalDate localDate);
}

package com.example.acacia.repository;

import com.example.acacia.dto.ContributionResponseDTO;
import com.example.acacia.model.Contribution;
import com.example.acacia.model.ContributionPeriod;
import com.example.acacia.model.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Repository
public interface ContributionRepository extends JpaRepository<Contribution,Long> {
    boolean existsByMemberAndPeriod(Member member, ContributionPeriod period);

    @Query("""
    SELECT COALESCE(SUM(c.amount), 0)
    FROM Contribution c
    WHERE c.member = :member
""")
    BigDecimal sumContributions(@Param("member") Member member);

    @Query("select sum(c.amount) from Contribution c")
    BigDecimal getSaccoBalance();

    @Query("select count (c) from Contribution c where c.member = :member and c.isLate = true")
    int countLate(Member member);

    @Query(
            value = """
        SELECT COUNT(*)
        FROM contributions c
          WHERE YEARWEEK(c.payment_date, 1) = YEARWEEK(CURDATE(), 1)
    """,
            nativeQuery = true
    )
    long countPaidThisWeek();

    @Query("""
    SELECT c FROM Contribution c
    WHERE c.paymentDate BETWEEN :start AND :end
""")
    List<Contribution> findByMonth(
            @Param("start") LocalDate start,
            @Param("end") LocalDate end
    );

    @Query("SELECT new com.example.acacia.dto.ContributionResponseDTO(" +
            "c.id, m.fullName, p.date, c.amount, c.paymentDate, c.isLate) " +
            "FROM Contribution c " +
            "JOIN c.member m " +
            "JOIN c.period p")
    List<ContributionResponseDTO> findAllFlattened();

    List<Contribution> findByPeriodIn(List<ContributionPeriod> periods);
}



package com.example.acacia.repository;

import com.example.acacia.enums.ExtraStatus;
import com.example.acacia.enums.ExtraType;
import com.example.acacia.model.Extra;
import com.example.acacia.model.Member;
import jakarta.persistence.Tuple;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExtraRepository extends JpaRepository<Extra, Long> {
    List<Extra> findByMemberAndTypeAndStatusOrderByIdAsc(Member member, ExtraType extraType, ExtraStatus extraStatus);

    boolean existsByMemberAndTypeAndStatus(Member member, ExtraType extraType, ExtraStatus extraStatus);

    List<Extra> findByMemberAndTypeAndStatus(Member member, ExtraType extraType, ExtraStatus extraStatus);

    @Query("select count(e) from Extra e where e.member = :member and e.type = :extraType and e.status = :extraStatus")
    int countActiveArrears(Member member, ExtraType extraType, ExtraStatus extraStatus);

    @Query("select count(e) from Extra e where e.status = :extraStatus and e.type = :extraType")
    long countMembersWithArrears(ExtraStatus extraStatus, ExtraType extraType);

    @Query("select e.id, m.fullName, e.amount, e.recordedDate, p.date, e.type, e.status from Extra e join e.period p join e.member m where e.type = :extraType")
    Page<Tuple> findExtras(Pageable pageable, ExtraType extraType);
}

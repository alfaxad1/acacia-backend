package com.example.acacia.repository;

import com.example.acacia.enums.ExtraStatus;
import com.example.acacia.enums.ExtraType;
import com.example.acacia.model.Extra;
import com.example.acacia.model.Member;
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
}

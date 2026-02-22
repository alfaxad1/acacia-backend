package com.example.acacia.repository;

import com.example.acacia.enums.MemberStatus;
import com.example.acacia.model.Member;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member,Long> {
    @Query("SELECT m.memberNumber FROM Member m ORDER BY m.id DESC")
    List<String> findAllMemberNumbers(Pageable pageable);

    default String findLastMemberNumber() {
        return findAllMemberNumbers(PageRequest.of(0, 1))
                .stream()
                .findFirst()
                .orElse(null);
    }

    List<Member> findAllByStatus(MemberStatus memberStatus);

    Member findAById(Long id);

    @Query("select count(m) from Member m where m.status = :status")
    long countActive(MemberStatus status);

    Optional<Member> findByEmail(String email);

}

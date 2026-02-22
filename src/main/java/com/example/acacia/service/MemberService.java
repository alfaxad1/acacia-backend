package com.example.acacia.service;

import com.example.acacia.model.Member;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

public interface MemberService {
    void addEditMember(Member member);

    List<Member> getMembers();

}

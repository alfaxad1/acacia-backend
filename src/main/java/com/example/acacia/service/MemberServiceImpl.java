package com.example.acacia.service;

import com.example.acacia.enums.MemberStatus;
import com.example.acacia.model.Member;
import com.example.acacia.repository.MemberRepository;
import com.example.acacia.utility.MemberNumberGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class MemberServiceImpl implements MemberService {
    private final MemberRepository memberRepository;
    private final MemberNumberGenerator memberNumberGenerator;
    @Override
    public void addEditMember(Member member) {
        if (member.getId()==null){
            Member member1 = new Member();
            member1.setMemberNumber(memberNumberGenerator.generateMemberNumber());
            member1.setFullName(member.getFullName());
            member1.setEmail(member.getEmail());
            member1.setPhone(member.getPhone());
            member1.setJoinDate(member.getJoinDate());
            memberRepository.save(member1);
        }else {
            Member member1 = memberRepository.findById(member.getId()).get();
            member1.setFullName(member.getFullName());
            member1.setEmail(member.getEmail());
            member1.setPhone(member.getPhone());
            member1.setJoinDate(member.getJoinDate());
            member1.setStatus(member.getStatus());
            memberRepository.save(member1);
        }
    }
}

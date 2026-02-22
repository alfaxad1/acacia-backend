package com.example.acacia.service;

import com.example.acacia.Exception.ApplicationException;
import com.example.acacia.Exception.AuthenticationException;
import com.example.acacia.SecurityConfig.JwtUtils;
import com.example.acacia.dto.LoginResponse;
import com.example.acacia.dto.UserData;
import com.example.acacia.model.Member;
import com.example.acacia.repository.MemberRepository;
import com.example.acacia.utility.MemberNumberGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final MemberRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final MemberRepository memberRepository;
    private final MemberNumberGenerator memberNumberGenerator;

    public void signup(Member member) {
        Optional<Member> members = userRepository.findByEmail(member.getEmail());
        if (members.isPresent()) {
            throw new ApplicationException("Member already exists");
        }
        Member member1 = new Member();
        member1.setMemberNumber(memberNumberGenerator.generateMemberNumber());
        member1.setFullName(member.getFullName());
        member1.setEmail(member.getEmail());
        member1.setPhone(member.getPhone());
        member1.setJoinDate(member.getJoinDate());
        member1.setPassword(passwordEncoder.encode(member.getPassword()));
        member1.setRole(member.getRole());
        member1.setActive(true);
        memberRepository.save(member1);
    }

    public LoginResponse login(String email, String password) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password)
            );
            Member user = (Member) authentication.getPrincipal();

            String token = jwtUtil.createAccessToken(user);
            String refreshToken = jwtUtil.createRefreshToken(user);
            Long accessTokenExpiration = jwtUtil.extractExpiration(token);

            return LoginResponse.builder()
                    .accessToken(token)
                    .refreshToken(refreshToken)
                    .expirationTime(accessTokenExpiration)
                    .userData(UserData.builder()
                            .memberId(jwtUtil.extractMemberId(token))
                            .name(jwtUtil.extractName(token))
                            .email(email)
                            .role(jwtUtil.extractRole(token))
                            .build())
                    .build();

        } catch (BadCredentialsException e) {
            throw new AuthenticationException("Invalid email or password");
        }
        catch (Exception e) {
            throw new ApplicationException("Error logging in:  " + e.getMessage());
        }
    }
}

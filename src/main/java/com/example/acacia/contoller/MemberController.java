package com.example.acacia.contoller;

import com.example.acacia.model.Member;
import com.example.acacia.service.AuthService;
import com.example.acacia.service.MemberService;
import com.example.acacia.utility.ResponseHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/member")
public class MemberController {
    @Autowired
    private MemberService memberService;

    @Autowired
    private AuthService authService;

    @PostMapping("/create")
    public ResponseEntity<?> addEditMember(@RequestBody Member member){
        authService.signup(member);
        return ResponseHandler.responseBuilder("member created successfully", HttpStatus.CREATED, null);
    }

    @GetMapping
    public ResponseEntity<?> getMembers(){
        List<Member> members = memberService.getMembers();
        return ResponseEntity.ok(members);
    }
}

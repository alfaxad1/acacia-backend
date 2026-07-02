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

    @PutMapping("/{id}")
    public ResponseEntity<?> updateMember(@PathVariable Long id, @RequestBody Member member){
        member.setId(id);
        memberService.addEditMember(member);
        return ResponseHandler.responseBuilder("member updated successfully", HttpStatus.OK, null);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteMember(@PathVariable Long id){
        memberService.deleteMember(id);
        return ResponseHandler.responseBuilder("member deleted successfully", HttpStatus.OK, null);
    }

    @GetMapping
    public ResponseEntity<?> getMembers(){
        List<Member> members = memberService.getMembers();
        return ResponseEntity.ok(members);
    }
}

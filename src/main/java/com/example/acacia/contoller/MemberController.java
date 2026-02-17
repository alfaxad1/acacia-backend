package com.example.acacia.contoller;

import com.example.acacia.model.Member;
import com.example.acacia.service.MemberService;
import com.example.acacia.utility.ResponseHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
@RequestMapping("/api/v1/member")
public class MemberController {
    @Autowired
    private MemberService memberService;

    @PostMapping
    public ResponseEntity<?> addEditMember(@RequestBody Member member){
        memberService.addEditMember(member);
        return ResponseHandler.responseBuilder("member created successfully", HttpStatus.CREATED, null);
    }
}

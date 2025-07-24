package com.kimbap.kbs.simjaejine.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kimbap.kbs.simjaejine.service.MemberService;
import com.kimbap.kbs.simjaejine.service.MemberVO;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class LoginController {

    private final MemberService memberService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody MemberVO member) {
        MemberVO user = memberService.getUserInfo(member.getId());
        System.out.println("로그인 요청 들어옴: " + member);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("아이디가 없습니다.");
        }

        if (!user.getPw().equals(member.getPw())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("비밀번호가 틀렸습니다.");
        }

        return ResponseEntity.ok(user);
    }

    
}
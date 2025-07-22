package com.kimbap.kbs.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kimbap.kbs.service.MemberService;
import com.kimbap.kbs.service.MemberVO;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class LoginController {

    private final MemberService memberService;

    // 로그인 요청 처리 예시 (POST /auth/login)
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestParam String id, @RequestParam String pw) {
        MemberVO user = memberService.getUserInfo(id);
        
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("아이디가 없습니다.");
        }
        
        if (!user.getPw().equals(pw)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("비밀번호가 틀렸습니다.");
        }
        
        // 로그인 성공 시, 필요한 사용자 정보 반환 (토큰 발급 등 추가 가능) // 자바 웹 토큰
        return ResponseEntity.ok(user);
    }
}
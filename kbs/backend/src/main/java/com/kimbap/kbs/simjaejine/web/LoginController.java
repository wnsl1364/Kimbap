package com.kimbap.kbs.simjaejine.web;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import com.kimbap.kbs.security.util.JwtUtil;
import com.kimbap.kbs.simjaejine.service.*;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class LoginController {

    private final MemberService memberService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

@PostMapping("/login")
public ResponseEntity<?> login(@RequestBody MemberVO loginRequest) {
    System.out.println("✅ login 요청 진입함"); // ✅ 이거 제일 먼저 찍어보기!

    MemberVO user = memberService.getUserInfo(loginRequest.getId());
    System.out.println("✅ 조회된 사용자: " + user); // null 여부 확인용

    if (user == null) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("아이디가 없습니다.");
    }

        // 비밀번호 검증 실패
        if (!passwordEncoder.matches(loginRequest.getPw(), user.getPw())) {
            // 실패 횟수 증가
            memberService.loginFailure(LoginSecurityVO.builder().memCd(user.getMemCd()).build());

            // 실패 후 즉시 재조회하여 잠금 여부 확인
            user = memberService.getUserInfo(loginRequest.getId());
            if ("f2".equals(user.getIdUsed())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("계정이 잠금 상태입니다.");
            }

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("비밀번호가 틀렸습니다.");
        }

        // 계정 잠금 상태 확인
        if ("f2".equals(user.getIdUsed())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("계정이 잠금 상태입니다. 관리자에게 문의하세요.");
        }

        // 로그인 성공 처리
        memberService.loginSuccess(LoginSecurityVO.builder().memCd(user.getMemCd()).build());

        // JWT 생성
        String token = jwtUtil.generateToken(user.getId());

        // 비밀번호는 응답에서 제거
        user.setPw(null);

        // 토큰 + 사용자 정보 반환
        return ResponseEntity.ok(Map.of(
                "token", token,
                "user", user
        ));
    }

    @PostMapping("/memberAdd")
    public ResponseEntity<?> registerMember(@RequestBody MemberAddVO memberAddVO) {
        try {
            if (memberService.idCheck(memberAddVO.getId())) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("이미 사용 중인 아이디입니다.");
            }

            memberService.addMember(memberAddVO);
            return ResponseEntity.ok("회원등록 성공");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("등록 중 오류 발생: " + e.getMessage());
        }
    }

    @GetMapping("/empList")
    public ResponseEntity<List<EmpCpCheckVO>> getEmpList() {
        return ResponseEntity.ok(memberService.getEmpList());
    }

    @GetMapping("/cpList")
    public ResponseEntity<List<EmpCpCheckVO>> getCpList() {
        return ResponseEntity.ok(memberService.getCpList());
    }
}

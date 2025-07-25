package com.kimbap.kbs.simjaejine.web;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kimbap.kbs.simjaejine.service.EmpCpCheckVO;
import com.kimbap.kbs.simjaejine.service.MemberAddVO;
import com.kimbap.kbs.simjaejine.service.MemberService;
import com.kimbap.kbs.simjaejine.service.MemberVO;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class LoginController {

    private final MemberService memberService;

    // 로그인
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
        System.out.println("유저에게 전달할 정보: " + user);
        return ResponseEntity.ok(user);
    }

    // 회원 등록
    @PostMapping("/memberAdd")
    public ResponseEntity<?> registerMember(@RequestBody MemberAddVO memberAddVO) {
        try {
            // 1. 사원 insert
            memberService.addMember(memberAddVO);

            // 성공 응답
            return ResponseEntity.ok("회원등록 성공");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("등록 중 오류 발생: " + e.getMessage());
        }
    }

    // 업체 리스트 조회
    @GetMapping("/empList")
    public ResponseEntity<List<EmpCpCheckVO>> getEmpList() {
        List<EmpCpCheckVO> list = memberService.getEmpList();
        return ResponseEntity.ok(list);
    }

    // 거래처 리스트 조회
    @GetMapping("/cpList")
    public ResponseEntity<List<EmpCpCheckVO>> getCpList() {
        List<EmpCpCheckVO> list = memberService.getCpList();
        return ResponseEntity.ok(list);
    }
}
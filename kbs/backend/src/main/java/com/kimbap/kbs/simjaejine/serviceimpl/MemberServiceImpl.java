package com.kimbap.kbs.simjaejine.serviceimpl;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kimbap.kbs.simjaejine.mapper.MemberMapper;
import com.kimbap.kbs.simjaejine.service.EmpCpCheckVO;
import com.kimbap.kbs.simjaejine.service.LoginSecurityVO;
import com.kimbap.kbs.simjaejine.service.MemberAddVO;
import com.kimbap.kbs.simjaejine.service.MemberService;
import com.kimbap.kbs.simjaejine.service.MemberVO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {
    private final MemberMapper memberMapper;
    private final PasswordEncoder passwordEncoder;

    // 로그인
    @Override
    public MemberVO getUserInfo(String id) {
        return memberMapper.getUserInfo(id);
    }

    @Override
    public void loginSuccess(LoginSecurityVO vo) {
        memberMapper.loginSuccess(vo);   // 실패 횟수 초기화
        memberMapper.recentLogin(vo);    // 최근 로그인 시간 갱신
    }

    @Override
    public void loginFailure(LoginSecurityVO vo) {
        memberMapper.loginFailure(vo);   // 실패 횟수 증가 및 잠금 처리
    }

    // 회원등록
    @Transactional
    @Override
    public int addMember(MemberAddVO memberAddVO) {
        // 비밀번호 암호화
        String rawPw = memberAddVO.getPw();
        String encodedPw = passwordEncoder.encode(rawPw);
        memberAddVO.setPw(encodedPw);

        // member 테이블 등록 (selectKey로 memCd 생성됨)
        int result = memberMapper.addMember(memberAddVO);

        // login_security 테이블 등록 (위에서 생성된 memCd 사용)
        memberMapper.memberAddSecurity(memberAddVO);

        return result;
    }

    // 업체 출력    
    @Override
    public List<EmpCpCheckVO> getEmpList() {
        return memberMapper.getEmpList();
    }

    // 거래처 출력
    @Override
    public List<EmpCpCheckVO> getCpList() {
        return memberMapper.getCpList();
    }
    
    // 아이디 중복체크
    @Override
    public boolean idCheck(String id) {
        return memberMapper.idCheck(id) > 0;
    }

    // 회원등록 비밀번호 암호화
}

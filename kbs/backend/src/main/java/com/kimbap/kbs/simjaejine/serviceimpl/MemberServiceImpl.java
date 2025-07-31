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

    @Override
    public MemberVO getUserInfo(String id) {
        // 1. 사용자 기본 정보 조회
        MemberVO user = memberMapper.getUserInfo(id);

        // 2. 사용자 권한 조회 및 VO에 세팅
        if (user != null) {
            List<String> roles = memberMapper.selectRolesByMemberId(user.getMemCd());
            user.setAuthorities(roles); // ✅ 권한 주입
        }

        // 3. 컨트롤러로 반환
        return user;
    }

    @Override
    public List<String> selectRolesByMemberId(String memCd) {
        return memberMapper.selectRolesByMemberId(memCd);
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

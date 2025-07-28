package com.kimbap.kbs.simjaejine.serviceimpl;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kimbap.kbs.simjaejine.mapper.MemberMapper;
import com.kimbap.kbs.simjaejine.service.EmpCpCheckVO;
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

    // 회원등록
    @Transactional
    @Override
    public int addMember(MemberAddVO memberAddVO) {
        String rawPw = memberAddVO.getPw();
        String encodedPw = passwordEncoder.encode(rawPw);
        memberAddVO.setPw(encodedPw);

        return memberMapper.addMember(memberAddVO);
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

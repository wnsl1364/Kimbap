package com.kimbap.kbs.simjaejine.service;

import java.util.List;

public interface MemberService {
    // 로그인
    MemberVO getUserInfo(String id);

    // 회원정보등록
    int addMember(MemberAddVO memberAddVO);

    // 업체 출력
    List<EmpCpCheckVO> getEmpList();

    // 거래처 출력
    List<EmpCpCheckVO> getCpList();

    // 아이디 중복 검사
    boolean idCheck(String id);

        // ✅ 로그인 성공 시 처리
    void loginSuccess(LoginSecurityVO vo);

    // ✅ 로그인 실패 시 처리
    void loginFailure(LoginSecurityVO vo);
    
    // 🔽 권한(role) 리스트 조회
    List<String> selectRolesByMemberId(String memCd);

}
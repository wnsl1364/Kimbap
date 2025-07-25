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

}
package com.kimbap.kbs.simjaejine.service;

public interface MemberService {
    // 로그인
    MemberVO getUserInfo(String id);

    // 회원정보등록
    MemberAddVO addMember(MemberAddVO memberAddVO);

    // 사원기준정보등록
    EmployeeAddVO addEmployee(EmployeeAddVO employeeAddVO);
}
package com.kimbap.kbs.simjaejine.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.kimbap.kbs.simjaejine.service.EmployeeAddVO;
import com.kimbap.kbs.simjaejine.service.MemberAddVO;
import com.kimbap.kbs.simjaejine.service.MemberVO;

@Mapper
public interface MemberMapper {
	// 로그인 및 로그인정보
	public MemberVO getUserInfo(String id);

	// 회원정보등록
	public int addMember(MemberAddVO memberAddVO);

	// 사원기준정보등록
	public int addEmployee(EmployeeAddVO employeeAddVO);
}

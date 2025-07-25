package com.kimbap.kbs.simjaejine.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.kimbap.kbs.simjaejine.service.EmpCpCheckVO;
import com.kimbap.kbs.simjaejine.service.MemberAddVO;
import com.kimbap.kbs.simjaejine.service.MemberVO;

@Mapper
public interface MemberMapper {
	// 로그인 및 로그인정보
	public MemberVO getUserInfo(String id);

	// 회원정보 등록
	public int addMember(MemberAddVO memberAddVO);

	// 업체 출력
	List<EmpCpCheckVO> getEmpList();

	// 거래처 출력
	List<EmpCpCheckVO> getCpList();


}

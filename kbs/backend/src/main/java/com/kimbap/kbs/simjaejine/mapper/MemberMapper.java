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

	// 회원정보 등록시 보안테이블 데이터 생성
	public int memberAddSecurity(MemberAddVO memberAddVO);

	// 업체 출력
	List<EmpCpCheckVO> getEmpList();

	// 거래처 출력
	List<EmpCpCheckVO> getCpList();

	// 아이디 중복 검사
	int idCheck(String id);

}

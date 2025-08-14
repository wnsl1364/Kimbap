package com.kimbap.kbs.simjaejine.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.kimbap.kbs.simjaejine.service.EmpCpCheckVO;
import com.kimbap.kbs.simjaejine.service.LoginSecurityVO;
import com.kimbap.kbs.simjaejine.service.MemberAddVO;
import com.kimbap.kbs.simjaejine.service.MemberVO;

@Mapper
public interface MemberMapper {
	// 로그인 및 로그인정보
	public MemberVO getUserInfo(String id);

	// 로그인 성공 시 (로그인 보안 초기화)
	int loginSuccess(LoginSecurityVO loginSecurityVO);

	// 로그인 실패 시 (실패 횟수 증가 및 잠금 처리)
	int loginFailure(LoginSecurityVO loginSecurityVO);

	// 최근 로그인 시간 업데이트
	int recentLogin(LoginSecurityVO loginSecurityVO);

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

  // 🔽 권한(role) 리스트 조회
  List<String> selectRolesByMemberId(@Param("username") String username);
}

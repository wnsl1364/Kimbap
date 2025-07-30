package com.kimbap.kbs.standard.mapper;

import java.util.List;

import com.kimbap.kbs.standard.service.CompanyVO;

public interface CompanyMapper {
    List<CompanyVO> getCompanyList(); // 거래처 목록 조회
    void insertCp(CompanyVO cp); // 거래처 등록
    CompanyVO getCpDetail(String cpCd); // 거래처 단건조회
    int existsCpcode(String cpCd);  // 거래처코드 존재 여부 확인
    List<CompanyVO> selectCpHistory(String cpCd); // 거래처 기준정보 이력조회
    CompanyVO selectLatestVersion(String cpCd); // 최신 버전 조회
    int disableOldVersion(String cpCd); // 기존 버전 비활성화
    int getNextRawCompanyCodeBySeq(); // 시퀀스 

    List<CompanyVO> getSupplierList(); // 공급업체 목록 조회
}
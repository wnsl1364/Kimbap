package com.kimbap.kbs.standard.service;

import java.util.List;
import java.util.Map;

public interface CompanyService {
    List<CompanyVO> getCompanyList(); // 거래처 목록조회
    void insertCp(CompanyVO cp); // 거래처 등록
    void updateCp(CompanyVO cp); // 거래처 수정
    Map<String, Object> getCompanyDetail(String cpCd); // 거래처 단건 조회
    List<CompanyVO> getSupplierList(); // 공급업체 목록조회
}
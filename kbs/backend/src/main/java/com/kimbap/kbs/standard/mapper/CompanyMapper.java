package com.kimbap.kbs.standard.mapper;

import java.util.List;

import com.kimbap.kbs.standard.service.CompanyVO;

public interface CompanyMapper {
    List<CompanyVO> getCompanyList(); // 거래처 목록 조회

     List<CompanyVO> getSupplierList(); // 공급업체 목록 조회
}
package com.kimbap.kbs.standard.service;

import java.util.List;

public interface CompanyService {
    List<CompanyVO> getCompanyList(); // 거래처 목록조회
    List<CompanyVO> getSupplierList(); // 공급업체 목록조회
}
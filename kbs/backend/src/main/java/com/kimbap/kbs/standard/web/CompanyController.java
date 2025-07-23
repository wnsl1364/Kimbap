package com.kimbap.kbs.standard.web;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kimbap.kbs.standard.service.CompanyService;
import com.kimbap.kbs.standard.service.CompanyVO;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/std/cp")
public class CompanyController {
    
    @Autowired
    private CompanyService companyService;

    // 거래처 목록 조회
    @GetMapping("/list")
    public List<CompanyVO> getCompanyList(){
        return companyService.getCompanyList();
    }

    // 공급업체 목록 조회
    @GetMapping("/sup/list")
    public List<CompanyVO> getSupplierList(){
        return companyService.getSupplierList();
    }
}

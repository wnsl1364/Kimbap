package com.kimbap.kbs.standard.serviceimpl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.kimbap.kbs.standard.mapper.CompanyMapper;
import com.kimbap.kbs.standard.service.CompanyService;
import com.kimbap.kbs.standard.service.CompanyVO;

@Service
public class CompanyServiceImpl implements CompanyService {

    @Autowired
    private CompanyMapper companyMapper;

    @Override
    public List<CompanyVO> getCompanyList() {
        return companyMapper.getCompanyList();
    }

    @Override
    public List<CompanyVO> getSupplierList(){
        return companyMapper.getSupplierList();
    }
}

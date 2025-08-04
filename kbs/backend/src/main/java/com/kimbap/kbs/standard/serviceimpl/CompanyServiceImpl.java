package com.kimbap.kbs.standard.serviceimpl;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kimbap.kbs.standard.mapper.CompanyMapper;
import com.kimbap.kbs.standard.service.ChangeItemVO;
import com.kimbap.kbs.standard.service.CompanyService;
import com.kimbap.kbs.standard.service.CompanyVO;

@Service
public class CompanyServiceImpl implements CompanyService {

    @Autowired
    private CompanyMapper companyMapper;

    // 거래처 목록 조회
    @Override
    public List<CompanyVO> getCompanyList() {
        return companyMapper.getCompanyList();
    }

    // 공급업체 목록 조회
    @Override
    public List<CompanyVO> getSupplierList(){
        return companyMapper.getSupplierList();
    }
    // 매출업체 목록 조회
    @Override
    public List<CompanyVO> getSalesList(){
        return companyMapper.getSalesList();
    }
    
    @Transactional
    @Override
    public void insertCp(CompanyVO cp) {
        // 1. 시퀀스를 이용한 거래처 코드 생성
        int next = companyMapper.getNextRawCompanyCodeBySeq();
        String cpCd;

        // 거래처 유형에 따라 코드 생성
        String cpType = cp.getCpType(); // 예: "j1", "j2"

        if ("j1".equalsIgnoreCase(cpType)) {
            if (next > 99) {
                throw new RuntimeException("j1 코드 범위 초과 (CP-006 ~ CP-099)");
            }
            cpCd = String.format("CP-%03d", next); // 예: CP-006, CP-007, ...
        } else if ("j2".equalsIgnoreCase(cpType)) {
            int j2Code = next + 100;
            if (j2Code > 199) {
                throw new RuntimeException("j2 코드 범위 초과 (CP-106 ~ CP-199)");
            }
            cpCd = String.format("CP-%03d", j2Code); // 예: CP-106, CP-107, ...
        } else {
            throw new RuntimeException("지원하지 않는 거래처 유형: " + cpType);
        }

        System.out.println("생성된 cpCd: " + cpCd);

        // 2. 중복 여부 확인
        if (companyMapper.existsCpcode(cpCd) > 0) {
            throw new RuntimeException("이미 존재하는 거래처 코드: " + cpCd);
        }

        // 3. 거래처 코드 & 기본 버전 설정
        cp.setCpCd(cpCd);

        // 4. 등록자 정보
        if (cp.getRegi() == null || cp.getRegi().isEmpty()) {
            cp.setRegi("admin");
        }

        // 5. 등록 수행
        companyMapper.insertCp(cp);
        System.out.println("등록되는 VO : " + cp);
    }

    // 거래처 수정
    @Transactional
    @Override
    public void updateCp(CompanyVO cp) {
        if (companyMapper.existsCpcode(cp.getCpCd()) == 0) {
            throw new IllegalArgumentException("존재하지 않는 거래처 코드입니다: " + cp.getCpCd());
        }

        // modi (수정자) 기본값 세팅
        if (cp.getModi() == null || cp.getModi().isEmpty()) {
            cp.setModi("admin");
        }

        companyMapper.updateCp(cp);
    }


    // 거래처 단건 조회
    @Override
        public Map<String, Object> getCompanyDetail(String cpCd) {
        Map<String, Object> result = new HashMap<>();

        CompanyVO company = companyMapper.getCpDetail(cpCd);

        result.put("company", company);

        return result;
    }
}

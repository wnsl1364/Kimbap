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
    
    // 거래처 등록
    @Transactional
    @Override
    public void insertCp(CompanyVO cp){
        // 1. 시퀀스를 이용한 거래처 코드 생성
        int next = companyMapper.getNextRawCompanyCodeBySeq();
        String cpCd = "CP-" + next;
        System.out.println("생성된 cpCd:" + cpCd);

        // 2. 중복 여부 확인
        if (companyMapper.existsCpcode(cpCd) > 0) {
            throw new RuntimeException("이미 존재하는 거래처 코드: " + cpCd);
        }

        // 3. 거래처 코드 & 기본 버전 설정
        cp.setCpCd(cpCd);

        if (cp.getCpVerCd() == null || cp.getCpVerCd().isEmpty()) {
            cp.setCpVerCd("V001");
        }

        // 4. 등록자 정보
        if (cp.getRegi() == null || cp.getRegi().isEmpty()){
            cp.setRegi("admin");
        }

        // 5.등록 수행
        companyMapper.insertCp(cp);
        System.out.println("등록되는 VO : " + cp);
    }

    // 거래처 수정
    @Override
    public void updateCp(CompanyVO newCp){
        // 1. 기존 최신 버전 조회
        CompanyVO oldCp = companyMapper.selectLatestVersion(newCp.getCpCd());
        if (oldCp == null) {
            throw new RuntimeException("존재하지 않는 거래처코드: " + newCp.getCpCd());
        }

        // 2.기존 버전 비활성화 처리
        companyMapper.disableOldVersion(newCp.getCpCd());

        // 3. 버전 증가
        String nextVer = getNextVersion(oldCp.getCpVerCd());
        newCp.setCpVerCd(nextVer);

        // 4. 필수 필드 세팅
        newCp.setIsUsed("f1");
        newCp.setRegDt(Timestamp.valueOf(LocalDateTime.now()));
        newCp.setModi("admin");
        newCp.setRegi(oldCp.getRegi());

        // 5. 거래처 등록 
        companyMapper.insertCp(newCp);

        System.out.println("버전 증가된 거래처 등록: " + newCp);


    }

    // 버전 코드 생성 함수 (V001 -> V002)
    private String getNextVersion(String currentVer) {
        int verNum = Integer.parseInt(currentVer.replace("V", ""));
        return String.format("V%03d", verNum + 1);
        
    }

    // 거래처 이력 조회
    @Override
    public List<CompanyVO> selectCpHistory(String cpCd) {
        return companyMapper.selectCpHistory(cpCd);
    }

    // 변경 사항 조회
    @Override
     public List<ChangeItemVO> getChangeHistory(String cpCd) {
        List<CompanyVO> histories = companyMapper.selectCpHistory(cpCd);
        List<ChangeItemVO> changeItems = new ArrayList<>();
        

        for (int i = 0; i < histories.size() - 1; i++){
            CompanyVO current = histories.get(i);
            CompanyVO prev = histories.get(i + 1);
            
            if (!Objects.equals(current.getCpName(), prev.getCpName())) {
                changeItems.add(new ChangeItemVO("거래처명", prev.getCpName(), current.getCpName(),
                 current.getChaRea(), current.getCpVerCd(), current.getRegDt(), current.getModi()));
            }
            if (!Objects.equals(current.getRepname(), prev.getRepname())) {
                changeItems.add(new ChangeItemVO("대표자명", prev.getRepname(), current.getRepname(),
                 current.getChaRea(), current.getCpVerCd(), current.getRegDt(), current.getModi()));
            }
            if (!Objects.equals(current.getTel(), prev.getTel())) {
                changeItems.add(new ChangeItemVO("연락처", prev.getTel(), current.getTel(),
                 current.getChaRea(), current.getCpVerCd(), current.getRegDt(), current.getModi()));
            }
            if (!Objects.equals(current.getCpEmail(), prev.getCpEmail())) {
                changeItems.add(new ChangeItemVO("이메일", prev.getCpEmail(), current.getCpEmail(),
                 current.getChaRea(), current.getCpVerCd(), current.getRegDt(), current.getModi()));
            }
            if (!Objects.equals(current.getLoanTerm(), prev.getLoanTerm())) {
                String before = prev.getLoanTerm() == null ? "-" : prev.getLoanTerm().toString();
                String after  = current.getLoanTerm() == null ? "-" : current.getLoanTerm().toString();

                changeItems.add(new ChangeItemVO("여신기간(일)", before, after,
                current.getChaRea(), current.getCpVerCd(), current.getRegDt(), current.getModi()));
            }
            if (!Objects.equals(current.getMname(), prev.getMname())) {
                changeItems.add(new ChangeItemVO("담당자명", prev.getMname(), current.getMname(),
                 current.getChaRea(), current.getCpVerCd(), current.getRegDt(), current.getModi()));
            }
            if (!Objects.equals(current.getAddress(), prev.getAddress())) {
                changeItems.add(new ChangeItemVO("주소", prev.getAddress(), current.getAddress(),
                 current.getChaRea(), current.getCpVerCd(), current.getRegDt(), current.getModi()));
            }
        }

        // 최초 등록 이력 V001은 항상 넣기
        if (!histories.isEmpty()) {
            CompanyVO first = histories.get(histories.size() - 1); // 마지막이 V001
            changeItems.add(new ChangeItemVO(
            "초기등록", "-", "-", first.getChaRea() != null ? first.getChaRea() : "-",
            first.getCpVerCd(),
            first.getRegDt(),
            first.getModi()
        ));
        }

        return changeItems;
        
     }

     @Override
        public Map<String, Object> getCompanyDetail(String cpCd) {
        Map<String, Object> result = new HashMap<>();

        CompanyVO company = companyMapper.getCpDetail(cpCd);

        result.put("company", company);

        return result;
    }
}

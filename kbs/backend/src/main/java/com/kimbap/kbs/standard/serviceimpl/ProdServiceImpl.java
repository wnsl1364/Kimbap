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

import com.kimbap.kbs.standard.mapper.ProdMapper;
import com.kimbap.kbs.standard.service.ChangeItemVO;
import com.kimbap.kbs.standard.service.ProdService;
import com.kimbap.kbs.standard.service.ProdVO;

@Service
public class ProdServiceImpl implements ProdService{

    @Autowired
    private ProdMapper prodMapper;

    @Override
    public List<ProdVO> getProdList() {
      return prodMapper.getProdList();
    }

    @Transactional
    @Override
    public void insertProd(ProdVO prod) {
        // 1. 시퀀스를 이용한 제품코드 생성
        int next = prodMapper.getNextRawProductCodeBySeq();
        String pcode = "PROD-" + next;
        System.out.println("생성된 pcode: " + pcode);

        // 2. 중복 여부 확인
        if (prodMapper.existsPcode(pcode) > 0) {
            throw new RuntimeException("이미 존재하는 제품코드: " + pcode);
        }

        // 3. 제품 코드 & 기본 버전 설정
        prod.setPcode(pcode);

        if (prod.getProdVerCd() == null || prod.getProdVerCd().isEmpty()) {
            prod.setProdVerCd("V001");
        }

        // 4. 등록자 정보 (임시)
        if (prod.getRegi() == null || prod.getRegi().isEmpty()) {
            prod.setRegi("admin"); // TODO: 로그인 유저 세팅
        }

        // 5. 등록 수행
        prodMapper.insertProd(prod);
        System.out.println("등록되는 VO : " + prod);
    }

    @Override
    public void updateProduct(ProdVO newProd) {
        // 1. 기존 최신 버전 조회
        ProdVO oldProd = prodMapper.selectLatestVersion(newProd.getPcode());
        if (oldProd == null) {
            throw new RuntimeException("존재하지 않는 제품코드: " + newProd.getPcode());
        }

        // 2. 기존 버전 비활성화 처리
        prodMapper.disableOldVersion(newProd.getPcode());

        // 3. 버전 증가
        String nextVer = getNextVersion(oldProd.getProdVerCd());
        newProd.setProdVerCd(nextVer);

        // 4. 필수 필드 세팅
        newProd.setIsUsed("f1");
        newProd.setRegDt(Timestamp.valueOf(LocalDateTime.now()));
        newProd.setModi("admin"); // TODO: 현재 로그인 사용자로 대체
        newProd.setRegi(oldProd.getRegi()); // 기존 등록자 그대로 유지

        // 5. 제품 등록 (insert = 버전 신규 생성)
        prodMapper.insertProd(newProd);

        System.out.println("🔁 버전 증가된 제품 등록: " + newProd);
    }

    @Override
    public List<ProdVO> selectProdHistory(String pcode) {
        return prodMapper.selectProdHistory(pcode);
    }

    @Override
    public List<ChangeItemVO> getChangeHistory(String pcode) {
        List<ProdVO> histories = prodMapper.selectProdHistory(pcode);
        List<ChangeItemVO> changeItems = new ArrayList<>();

        for (int i = 0; i < histories.size() - 1; i++) {
            ProdVO current = histories.get(i);
            ProdVO prev = histories.get(i + 1);

            if (!Objects.equals(current.getProdName(), prev.getProdName())) {
                changeItems.add(new ChangeItemVO("제품명", prev.getProdName(), current.getProdName(),
                        current.getChaRea(), current.getProdVerCd(), current.getRegDt(), current.getModi()));
            }

            if (!Objects.equals(current.getWei(), prev.getWei())) {
                changeItems.add(new ChangeItemVO("중량", prev.getWei(), current.getWei(),
                        current.getChaRea(), current.getProdVerCd(), current.getRegDt(), current.getModi()));
            }
            if (!Objects.equals(current.getEdate(), prev.getEdate())) {
                changeItems.add(new ChangeItemVO("소비기한",
                        prev.getEdate() == null ? null : prev.getEdate().toString(),
                        current.getEdate() == null ? null : current.getEdate().toString(),
                        current.getChaRea(), current.getProdVerCd(), current.getRegDt(), current.getModi()));
            }
            if (!Objects.equals(current.getStoTemp(), prev.getStoTemp())) {
                changeItems.add(new ChangeItemVO("보관온도", prev.getStoTemp(), current.getStoTemp(),
                        current.getChaRea(), current.getProdVerCd(), current.getRegDt(), current.getModi()));
            }
            if (!Objects.equals(current.getPacUnit(), prev.getPacUnit())) {
                changeItems.add(new ChangeItemVO("포장단위", prev.getPacUnit(), current.getPacUnit(),
                        current.getChaRea(), current.getProdVerCd(), current.getRegDt(), current.getModi()));
            }
            if (!Objects.equals(current.getPrimeCost(), prev.getPrimeCost())) {
                changeItems.add(new ChangeItemVO("원가",
                        prev.getPrimeCost() == null ? null : prev.getPrimeCost().toString(),
                        current.getPrimeCost() == null ? null : current.getPrimeCost().toString(),
                        current.getChaRea(), current.getProdVerCd(), current.getRegDt(), current.getModi()));
            }
            if (!Objects.equals(current.getProdUnitPrice(), prev.getProdUnitPrice())) {
                changeItems.add(new ChangeItemVO("제품단가",
                        prev.getProdUnitPrice() == null ? null : prev.getProdUnitPrice().toString(),
                        current.getProdUnitPrice() == null ? null : current.getProdUnitPrice().toString(),
                        current.getChaRea(), current.getProdVerCd(), current.getRegDt(), current.getModi()));
            }
        }

        // 최초 등록 이력 V001은 항상 넣기
        if (!histories.isEmpty()) {
            ProdVO first = histories.get(histories.size() - 1); // 마지막이 V001
            changeItems.add(new ChangeItemVO(
                "-", "-", "-", "-",
                first.getProdVerCd(),
                first.getRegDt(),
                first.getModi()
            ));
        }

        return changeItems;
    }

    // 버전 코드 생성 함수 (V001 -> V002)
    private String getNextVersion(String currentVer) {
        int verNum = Integer.parseInt(currentVer.replace("V", ""));
        return String.format("V%03d", verNum + 1);
        
    }

    @Override
    public Map<String, Object> getProductDetail(String pcode) {
       Map<String, Object> result = new HashMap<>();

       ProdVO product = prodMapper.getProdDetail(pcode);

       result.put("product", product);

       return result;
    }


}

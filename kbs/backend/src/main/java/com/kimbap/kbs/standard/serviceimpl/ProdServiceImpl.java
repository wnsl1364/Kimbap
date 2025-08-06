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
import com.kimbap.kbs.standard.service.VersionSyncService;

@Service
public class ProdServiceImpl implements ProdService{

    @Autowired
    private ProdMapper prodMapper;

    @Autowired
    private VersionSyncService versionSyncService;

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

        // 2. 내용 변경 여부 확인 (isUsed는 비교 대상에서 제외)
        boolean isChanged =
                !Objects.equals(oldProd.getProdName(), newProd.getProdName()) ||
                !Objects.equals(oldProd.getWei(), newProd.getWei()) ||
                !Objects.equals(oldProd.getEdate(), newProd.getEdate()) ||
                !Objects.equals(oldProd.getStoTemp(), newProd.getStoTemp()) ||
                !Objects.equals(oldProd.getPacUnit(), newProd.getPacUnit()) ||
                !Objects.equals(oldProd.getPrimeCost(), newProd.getPrimeCost()) ||
                !Objects.equals(oldProd.getProdUnitPrice(), newProd.getProdUnitPrice());

        if (isChanged) {
            // ✅ 내용이 바뀐 경우 - 버전 증가 + 기존 비활성화
            prodMapper.disableOldVersion(newProd.getPcode());

            String nextVer = getNextVersion(oldProd.getProdVerCd());
            newProd.setProdVerCd(nextVer);
            newProd.setIsUsed("f1"); // 신규는 항상 사용
            newProd.setRegDt(Timestamp.valueOf(LocalDateTime.now()));
            newProd.setModi(newProd.getModi());
            newProd.setRegi(oldProd.getRegi()); // 등록자는 유지

            prodMapper.insertProd(newProd);

            // ✅ 🔥 참조 테이블 버전 동기화
            versionSyncService.syncProductVersion(
                newProd.getPcode(),
                oldProd.getProdVerCd(),
                nextVer
            );

            System.out.println("🆕 내용 변경 → 버전 증가: " + newProd);

        } else if (!Objects.equals(oldProd.getIsUsed(), newProd.getIsUsed())) {
            // ✅ 내용은 동일하고 사용여부만 바뀐 경우 - update만
            prodMapper.updateIsUsedOnly(oldProd.getPcode(), oldProd.getProdVerCd(), newProd.getIsUsed(), newProd.getModi());
            System.out.println("🛠 사용여부만 변경됨 → update: " + newProd.getIsUsed());
        } else {
            // ❌ 변경 없음
            System.out.println("❎ 변경 없음 → 아무것도 안함");
        }
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

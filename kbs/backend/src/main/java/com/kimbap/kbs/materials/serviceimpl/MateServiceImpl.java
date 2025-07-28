package com.kimbap.kbs.materials.serviceimpl;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kimbap.kbs.materials.mapper.MateMapper;
import com.kimbap.kbs.materials.service.MateService;
import com.kimbap.kbs.materials.service.MaterialsVO;
import com.kimbap.kbs.materials.service.SearchCriteria;

@Service
@Transactional
public class MateServiceImpl implements MateService {

    @Autowired
    private MateMapper mateMapper;

    @Override
    public void insertMateInbo(MaterialsVO mateInbo) {
        try {
            // ✅ INSERT 시에도 LOT 번호가 없으면 자동 생성
            if (mateInbo.getLotNo() == null || mateInbo.getLotNo().trim().isEmpty()) {
                String lotNumber = generateMaterialLotNumber(mateInbo.getMcode());
                mateInbo.setLotNo(lotNumber);
                System.out.println("INSERT 시 LOT 번호 자동 생성: " + lotNumber);
            }
            
            mateMapper.insertMateInbo(mateInbo);  // ✅ 올바른 INSERT 호출
            System.out.println("자재입고 등록 완료: " + mateInbo.getMateInboCd());
        } catch (Exception e) {
            System.err.println("자재입고 등록 실패: " + e.getMessage());
            throw new RuntimeException("자재입고 등록 실패: " + e.getMessage(), e);
        }
    }

    @Override
    public void updateMateInbo(MaterialsVO mateInbo) {
        try {
            // ✅ UPDATE 시에도 LOT 번호가 없으면 자동 생성 (입고처리 시)
            if (mateInbo.getLotNo() == null || mateInbo.getLotNo().trim().isEmpty()) {
                String lotNumber = generateMaterialLotNumber(mateInbo.getMcode());
                mateInbo.setLotNo(lotNumber);
                System.out.println("UPDATE 시 LOT 번호 자동 생성: " + lotNumber);
            }
            
            System.out.println("=== 자재입고 수정 요청 ===");
            System.out.println("mateInboCd: " + mateInbo.getMateInboCd());
            System.out.println("fcode: " + mateInbo.getFcode());
            System.out.println("facVerCd: " + mateInbo.getFacVerCd());
            System.out.println("inboStatus: " + mateInbo.getInboStatus());
            System.out.println("lotNo: " + mateInbo.getLotNo());
            
            mateMapper.updateMateInbo(mateInbo);  // ✅ 올바른 UPDATE 호출
            System.out.println("자재입고 수정 완료: " + mateInbo.getMateInboCd());
        } catch (Exception e) {
            System.err.println("자재입고 수정 실패: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("자재입고 수정 실패: " + e.getMessage(), e);
        }
    }
    
    /**
     * 자재 LOT 번호 생성
     * 규칙: LOT-품목유형-연월일-순번
     * 예: LOT-100-20250530-1, LOT-200-20250530-2
     */
    private String generateMaterialLotNumber(String mcode) {
        try {
            // 1. 현재 날짜 (yyyyMMdd)
            String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            
            // 2. 자재 정보 조회해서 품목 유형 확인
            String mateType = getMaterialType(mcode);
            String lotTypeCode = getLotTypeByMaterialType(mateType);
            
            // 3. 오늘 날짜의 해당 품목유형 LOT 개수 조회
            String lotPattern = "LOT-" + lotTypeCode + "-" + today + "-%";
            int existingCount = mateMapper.countLotsByPattern(lotPattern);
            
            // 4. 다음 시퀀스 = 기존 개수 + 1 (날짜별로 1부터 시작)
            int nextSequence = existingCount + 1;
            
            // 5. LOT 번호 생성: LOT-품목유형-연월일-순번
            String lotNumber = String.format("LOT-%s-%s-%d", lotTypeCode, today, nextSequence);
            
            System.out.println("=== LOT 번호 생성 과정 ===");
            System.out.println("자재코드: " + mcode);
            System.out.println("품목유형: " + mateType + " → 코드: " + lotTypeCode);
            System.out.println("오늘날짜: " + today);
            System.out.println("기존개수: " + existingCount + "개");
            System.out.println("다음순번: " + nextSequence);
            System.out.println("생성결과: " + lotNumber);
            
            return lotNumber;
            
        } catch (Exception e) {
            System.err.println("LOT 생성 실패, 임시 번호 사용: " + e.getMessage());
            e.printStackTrace();
            
            // 실패 시 임시 번호 생성
            String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            long timestamp = System.currentTimeMillis() % 1000;
            return String.format("LOT-TMP-%s-%d", today, timestamp);
        }
    }
    
    /**
     * 자재 코드로 품목 유형 조회
     */
    private String getMaterialType(String mcode) {
        try {
            String mateType = mateMapper.getMaterialType(mcode);
            System.out.println("자재코드 " + mcode + "의 품목유형: " + mateType);
            return mateType != null ? mateType : "h1"; // 기본값: 원자재
        } catch (Exception e) {
            System.err.println("자재 타입 조회 실패, 기본값(h1) 사용: " + e.getMessage());
            return "h1"; // 기본값: 원자재
        }
    }
    
    /**
     * 자재 유형을 LOT 타입 코드로 변환
     * h1 (원자재) → 100
     * h2 (부자재) → 200
     */
    private String getLotTypeByMaterialType(String mateType) {
        if (mateType == null) {
            System.out.println("품목유형이 null, 기본값(100) 사용");
            return "100"; // 기본값: 원자재
        }
        
        switch (mateType.toLowerCase()) {
            case "h1": 
                System.out.println("원자재(h1) → LOT 타입: 100");
                return "100";  // 원자재 (김, 쌀, 야채 등)
            case "h2": 
                System.out.println("부자재(h2) → LOT 타입: 200");
                return "200";  // 부자재 (포장용지, 포장박스)
            default: 
                System.out.println("알 수 없는 자재 유형 (" + mateType + "), 기본값(100) 사용");
                return "100"; // 알 수 없으면 원자재로 처리
        }
    }

    @Override
    public List<MaterialsVO> getMateInboList() {
        try {
            List<MaterialsVO> inboList = mateMapper.getMateInboList();
            System.out.println("=== 자재입고 목록 조회 결과 ===");
            
            if (inboList != null && !inboList.isEmpty()) {
                System.out.println("총 " + inboList.size() + "건 조회됨");
                
                // 첫 번째 데이터 로깅 (디버깅용)
                MaterialsVO firstItem = inboList.get(0);
                System.out.println("첫 번째 데이터:");
                System.out.println("  - purcCd: " + firstItem.getPurcCd());
                System.out.println("  - ordDt: " + firstItem.getOrdDt());
                System.out.println("  - regi: " + firstItem.getRegi());
                System.out.println("  - regiName: " + firstItem.getRegiName());  // ✅ 이게 중요!
                System.out.println("  - purcStatus: " + firstItem.getPurcStatus());
                System.out.println("  - inboStatus: " + firstItem.getInboStatus());
                System.out.println("  - mateName: " + firstItem.getMateName());
                System.out.println("  - cpName: " + firstItem.getCpName());
                
                // ✅ 모든 입고 대기 데이터의 담당자 정보도 확인
                System.out.println("=== 입고대기 상태 데이터들의 담당자 정보 ===");
                inboList.stream()
                    .filter(item -> "c3".equals(item.getInboStatus()))
                    .forEach(item -> {
                        System.out.println("입고코드: " + item.getMateInboCd() + 
                                         " | regi: " + item.getRegi() + 
                                         " | regiName: " + item.getRegiName());
                    });
                    
            } else {
                System.out.println("조회된 데이터가 없습니다.");
            }
            
            return inboList;
        } catch (Exception e) {
            System.err.println("자재입고 목록 조회 실패: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("자재입고 목록 조회 실패: " + e.getMessage(), e);
        }
    }

    @Override
    public MaterialsVO getMateInboById(String mateInboCd) {
        try {
            MaterialsVO mateInbo = mateMapper.getMateInboById(mateInboCd);
            System.out.println("자재입고 단건 조회 완료: " + mateInboCd);
            return mateInbo;
        } catch (Exception e) {
            System.err.println("자재입고 단건 조회 실패: " + e.getMessage());
            throw new RuntimeException("자재입고 단건 조회 실패: " + e.getMessage(), e);
        }
    }

    @Override
    public List<MaterialsVO> getPurcOrdList() {
        return mateMapper.getPurcOrdList();
    }

    @Override
    public List<MaterialsVO> getMateRelList() {
        return mateMapper.getMateRelList();
    }

    @Override
    public void insertMateRel(MaterialsVO mateRel) {
        mateMapper.insertMateRel(mateRel);
    }

    @Override
    public List<MaterialsVO> getPurchaseOrders(SearchCriteria criteria) {
        return mateMapper.getPurcOrdList(criteria);
    }
    
    @Override
    public List<MaterialsVO> getActiveFactoryList() {
        try {
            List<MaterialsVO> factoryList = mateMapper.getActiveFactoryList();
            System.out.println("=== 공장 목록 조회 결과 ===");
            
            if (factoryList != null && !factoryList.isEmpty()) {
                System.out.println("총 " + factoryList.size() + "개 공장 조회됨");
                
                for (MaterialsVO factory : factoryList) {
                    System.out.println("  - " + factory.getFcode() + " (" + factory.getFacVerCd() + "): " + factory.getFacName());
                }
            } else {
                System.out.println("조회된 공장이 없습니다.");
            }
            
            return factoryList;
        } catch (Exception e) {
            System.err.println("공장 목록 조회 실패: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("공장 목록 조회 실패: " + e.getMessage(), e);
        }
    }    
}
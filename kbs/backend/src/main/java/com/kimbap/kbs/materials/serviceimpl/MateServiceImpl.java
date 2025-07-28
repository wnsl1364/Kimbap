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
            // ✅ LOT 번호가 없으면 자동 생성
            if (mateInbo.getLotNo() == null || mateInbo.getLotNo().trim().isEmpty()) {
                String lotNumber = generateMaterialLotNumber(mateInbo.getMcode());
                mateInbo.setLotNo(lotNumber);
                System.out.println("LOT 번호 자동 생성: " + lotNumber);
            }
            
            // 기존 수정 로직
            mateMapper.updateMateInbo(mateInbo);
            
        } catch (Exception e) {
            System.err.println("자재입고 수정 실패: " + e.getMessage());
            throw new RuntimeException("자재입고 수정 실패: " + e.getMessage(), e);
        }
    }
      /**
     * 자재 LOT 번호 생성 (원자재 100, 부자재 200만)
     */
     private String generateMaterialLotNumber(String mcode) {
        try {
            // 1. 현재 날짜 (YYYYMMDD)
            String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            
            // 2. 자재 정보 조회해서 품목 유형 확인
            String mateType = getMaterialType(mcode);
            String lotType = getLotTypeByMaterialType(mateType);
            
            // 3. 오늘 날짜의 해당 품목유형 LOT 개수 조회
            String lotPattern = "LOT-" + lotType + "-" + today + "-%";
            int existingCount = mateMapper.countLotsByPattern(lotPattern);
            
            // 4. 다음 시퀀스 = 기존 개수 + 1
            int nextSequence = existingCount + 1;
            
            // 5. LOT 번호 생성: LOT-품목유형-연월일-순번
            String lotNumber = String.format("LOT-%s-%s-%d", lotType, today, nextSequence);
            
            System.out.println("자재코드: " + mcode + " → 품목유형: " + mateType + " → LOT: " + lotNumber);
            return lotNumber;
            
        } catch (Exception e) {
            System.err.println("LOT 생성 실패, 임시 번호 사용: " + e.getMessage());
            // 실패 시 임시 번호
            String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            return "LOT-TMP-" + today + "-" + (System.currentTimeMillis() % 1000);
        }
    }
        /**
     * 자재 코드로 품목 유형 조회
     */
    private String getMaterialType(String mcode) {
        try {
            // 자재 정보에서 mate_type 조회
            String mateType = mateMapper.getMaterialType(mcode);
            return mateType != null ? mateType : "h1"; // 기본값: 원자재
        } catch (Exception e) {
            System.err.println("자재 타입 조회 실패, 기본값(h1) 사용: " + e.getMessage());
            return "h1"; // 기본값: 원자재
        }
    }
    
    /**
     * 자재 유형을 LOT 타입으로 변환 (원자재/부자재만)
     */
    private String getLotTypeByMaterialType(String mateType) {
        if (mateType == null) {
            return "100"; // 기본값: 원자재
        }
        
        switch (mateType) {
            case "h1": return "100";  // 원자재 (김, 쌀, 야채 등)
            case "h2": return "200";  // 부자재 (포장용지, 포장박스)
            default: 
                System.out.println("알 수 없는 자재 유형 (" + mateType + "), 기본값(100) 사용");
                return "100"; // 알 수 없으면 원자재로 처리
        }
    }

    @Override
    public List<MaterialsVO> getMateInboList() {
        return mateMapper.getMateInboList();
    }

    @Override
    public void updateMateInbo(MaterialsVO mateInbo) {
        mateMapper.updateMateInbo(mateInbo);
    }

    @Override
    public MaterialsVO getMateInboById(String mateInboCd) {
        return mateMapper.getMateInboById(mateInboCd);
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
            return mateMapper.getActiveFactoryList();
        } catch (Exception e) {
            System.err.println("공장 목록 조회 실패: " + e.getMessage());
            throw new RuntimeException("공장 목록 조회 실패: " + e.getMessage(), e);
        }
    }    
}
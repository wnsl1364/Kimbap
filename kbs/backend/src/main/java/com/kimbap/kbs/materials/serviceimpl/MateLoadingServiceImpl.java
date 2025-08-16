package com.kimbap.kbs.materials.serviceimpl;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kimbap.kbs.materials.mapper.MateLoadingMapper;
import com.kimbap.kbs.materials.service.MateLoadingService;
import com.kimbap.kbs.materials.service.MateLoadingVO;

@Service
@Transactional
public class MateLoadingServiceImpl implements MateLoadingService {

    @Autowired
    private MateLoadingMapper mateLoadingMapper;

    @Override
    public List<MateLoadingVO> getAllMateLoadingWaitList() {
        List<MateLoadingVO> list = mateLoadingMapper.getAllMateLoadingWaitList();
        return list;
    }

    @Override
    public MateLoadingVO getMateLoadingByInboCd(String mateInboCd) {
        MateLoadingVO result = mateLoadingMapper.getMateLoadingByInboCd(mateInboCd);
        return result;
    }

    @Override
    public String processMateLoading(MateLoadingVO mateLoading) {
        
        // material 테이블에서 자재 정보 조회
        MateLoadingVO materialInfo = null;
        try {
            materialInfo = mateLoadingMapper.getMaterialInfo(mateLoading.getMcode());
        } catch (Exception e) {
            throw new RuntimeException("자재 정보 조회 실패: " + mateLoading.getMcode(), e);
        }
        
        if (materialInfo == null) {
            // 기본값으로 처리 계속 진행
            mateLoading.setItemType("h1"); // 기본값: 원자재 (품목유형 코드)
            // unit은 기존 값 유지
        } else {
            // mate_type을 item_type으로 변환 (H1 -> h1, H2 -> h2)
            String itemType = convertMateTypeToItemType(materialInfo.getMateType());
            mateLoading.setItemType(itemType);
            
            // unit을 material 테이블의 unit(공통코드)으로 설정
            mateLoading.setUnit(materialInfo.getUnit());
        }
        
        // 현재 시간 설정
        mateLoading.setInboDt(Timestamp.valueOf(LocalDateTime.now()));
        
        // 창고재고목록코드 생성
        String wslcode = generateWareStockCode();
        mateLoading.setWslcode(wslcode);
        
        // 등록자 설정 - 프론트엔드에서 전달된 empCd 사용
        if (mateLoading.getRegi() == null || mateLoading.getRegi().trim().isEmpty()) {
            mateLoading.setRegi("system"); // 기본값
        }
        
        // ware_stock 테이블에 적재 정보 저장
        try {
            mateLoadingMapper.insertWareStock(mateLoading);
            
            // loaded_qty 업데이트 및 상태 변경 (c8 적재중/c9 적재완료)
            mateLoadingMapper.updateLoadedQtyAndStatus(mateLoading.getMateInboCd(), mateLoading.getQty().intValue());
            
        } catch (Exception e) {
            throw e;
        }
        
        return "자재 적재 처리가 완료되었습니다.";
    }
    
    @Override
    public String processMateLoadingBatch(List<MateLoadingVO> mateLoadingList) {
        
        int successCount = 0;
        int failCount = 0;
        
        for (int i = 0; i < mateLoadingList.size(); i++) {
            MateLoadingVO mateLoading = mateLoadingList.get(i);
            
            try {
                // material 테이블에서 자재 정보 조회
                MateLoadingVO materialInfo = null;
                try {
                materialInfo = mateLoadingMapper.getMaterialInfo(mateLoading.getMcode());
            } catch (Exception e) {
                // 자재 정보 조회 실패 시 해당 항목은 건너뜀
            }                // 현재 시간 설정
                mateLoading.setInboDt(Timestamp.valueOf(LocalDateTime.now()));
                
                // 창고재고목록코드 생성
                String wslcode = generateWareStockCode();
                mateLoading.setWslcode(wslcode);
                
                // 등록자 설정 - 프론트엔드에서 전달된 empCd 사용
                if (mateLoading.getRegi() == null || mateLoading.getRegi().trim().isEmpty()) {
                    mateLoading.setRegi("system"); // 기본값
                }
                
                if (materialInfo == null) {
                    // 기본값으로 처리 계속 진행
                    mateLoading.setItemType("h1"); // 기본값: 원자재 (품목유형 코드)
                    // unit은 기존 값 유지
                } else {
                    // mate_type을 item_type으로 변환 (H1 -> h1, H2 -> h2)
                    String itemType = convertMateTypeToItemType(materialInfo.getMateType());
                    mateLoading.setItemType(itemType);
                    
                    // unit을 material 테이블의 unit(공통코드)으로 설정
                    mateLoading.setUnit(materialInfo.getUnit());
                }
                
                // ware_stock 테이블에 적재 정보 저장
                mateLoadingMapper.insertWareStock(mateLoading);
                
                // loaded_qty 업데이트 및 상태 변경 (c8 적재중/c9 적재완료)
                mateLoadingMapper.updateLoadedQtyAndStatus(mateLoading.getMateInboCd(), mateLoading.getQty().intValue());
                
                successCount++;
                
            } catch (Exception e) {
                failCount++;
            }
        }
        
        String result = String.format("다중 자재 적재 처리 완료 - 성공: %d건, 실패: %d건", 
                                     successCount, failCount);
        
        System.out.println("=== 다중 적재 처리 완료 ===");
        System.out.println(result);
        
        return result;
    }

    @Override
    public List<MateLoadingVO> getActiveFactoryList() {
        List<MateLoadingVO> factoryList = mateLoadingMapper.getActiveFactoryList();
        return factoryList;
    }

        @Override
    public List<MateLoadingVO> getWarehousesByFactory(String fcode) {
        try {
            List<MateLoadingVO> warehouseList = mateLoadingMapper.getWarehousesByFactory(fcode);
            System.out.println("공장별 창고 목록 조회 완료: " + fcode + " - " + warehouseList.size() + "개");
            return warehouseList;
        } catch (Exception e) {
            System.err.println("공장별 창고 목록 조회 실패: " + fcode + " - " + e.getMessage());
            throw new RuntimeException("공장별 창고 목록 조회 실패: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Map<String, Object>> getWarehouseAreasWithStock(String wcode, Integer floor) {
        try {
            // 해당 층의 구역 정보 조회
            List<MateLoadingVO> warehouseAreas = mateLoadingMapper.getWarehouseAreasByFloor(wcode, floor);
            
            List<Map<String, Object>> result = new ArrayList<>();
            
            for (MateLoadingVO area : warehouseAreas) {
                Map<String, Object> areaInfo = new HashMap<>();
                
                // 기본 구역 정보
                areaInfo.put("wareAreaCd", area.getWareAreaCd());
                areaInfo.put("areaRow", area.getAreaRow());
                areaInfo.put("areaCol", area.getAreaCol());
                areaInfo.put("areaFloor", area.getAreaFloor());
                areaInfo.put("vol", area.getVol());
                
                // 현재 적재량 조회
                Integer currentVolume = mateLoadingMapper.getCurrentVolumeByArea(area.getWareAreaCd());
                if (currentVolume == null) currentVolume = 0;
                
                areaInfo.put("currentVolume", currentVolume);
                areaInfo.put("availableVolume", area.getVol().intValue() - currentVolume);
                
                // 현재 적재된 자재 조회
                String currentMaterial = mateLoadingMapper.getCurrentMaterialByArea(area.getWareAreaCd());
                areaInfo.put("currentMaterial", currentMaterial);
                
                result.add(areaInfo);
            }
            
            System.out.println("창고 구역별 적재 현황 조회 완료: " + wcode + " " + floor + "층 - " + result.size() + "개 구역");
            return result;
            
        } catch (Exception e) {
            System.err.println("창고 구역별 적재 현황 조회 실패: " + wcode + " " + floor + "층 - " + e.getMessage());
            throw new RuntimeException("창고 구역별 적재 현황 조회 실패: " + e.getMessage(), e);
        }
    }

    @Override
    public String getWareAreaCode(String wcode, String areaRow, Integer areaCol, Integer areaFloor) {
        try {
            String wareAreaCode = mateLoadingMapper.getWareAreaCode(wcode, areaRow, areaCol, areaFloor);
            
            if (wareAreaCode == null || wareAreaCode.trim().isEmpty()) {
                // 창고구역코드가 존재하지 않으면 생성
                // W-001-B4-2 형식으로 생성
                String warehouseNumber = wcode.replace("WARE-", "");
                wareAreaCode = String.format("W-%s-%s%d-%d", warehouseNumber, areaRow, areaCol, areaFloor);
                System.out.println("창고구역코드 생성: " + wareAreaCode);
            }
            
            return wareAreaCode;
            
        } catch (Exception e) {
            System.err.println("창고구역코드 조회/생성 실패: " + e.getMessage());
            throw new RuntimeException("창고구역코드 조회/생성 실패: " + e.getMessage(), e);
        }
    }

    @Override
    public Map<String, Object> validateAreaAllocation(String wareAreaCd, String mcode, Integer allocateQty) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 현재 적재량 조회
            Integer currentVolume = mateLoadingMapper.getCurrentVolumeByArea(wareAreaCd);
            if (currentVolume == null) currentVolume = 0;
            
            // 구역 정보 조회 (용량 확인용)
            MateLoadingVO areaStock = mateLoadingMapper.getWarehouseAreaStock(wareAreaCd);
            
            if (areaStock == null) {
                result.put("isValid", false);
                result.put("message", "해당 구역 정보를 찾을 수 없습니다.");
                return result;
            }
            
            // 현재 적재된 자재 확인
            String currentMaterial = mateLoadingMapper.getCurrentMaterialByArea(wareAreaCd);
            
            // 다른 자재가 적재되어 있는지 확인
            if (currentMaterial != null && !currentMaterial.equals(mcode)) {
                result.put("isValid", false);
                result.put("message", "해당 구역에는 다른 자재(" + currentMaterial + ")가 적재되어 있습니다.");
                result.put("currentMaterial", currentMaterial);
                return result;
            }
            
            // 용량 확인 - 실제 구역 용량 조회
            int maxVolume = (areaStock.getVol() != null) ? areaStock.getVol().intValue() : 100; // 기본값 100
            int availableVolume = maxVolume - currentVolume;
            
            if (allocateQty > availableVolume) {
                result.put("isValid", false);
                result.put("message", String.format("구역 용량이 부족합니다. 잔여용량: %d, 요청수량: %d", 
                                                   availableVolume, allocateQty));
                result.put("availableVolume", availableVolume);
                result.put("currentVolume", currentVolume);
                result.put("maxVolume", maxVolume);
                return result;
            }
            
            // 검증 통과
            result.put("isValid", true);
            result.put("message", "적재 가능합니다.");
            result.put("availableVolume", availableVolume);
            result.put("currentVolume", currentVolume);
            result.put("maxVolume", maxVolume);
            result.put("currentMaterial", currentMaterial);
            
            return result;
            
        } catch (Exception e) {
            System.err.println("구역 적재 검증 실패: " + wareAreaCd + " - " + e.getMessage());
            result.put("isValid", false);
            result.put("message", "구역 적재 검증 중 오류가 발생했습니다: " + e.getMessage());
            return result;
        }
    }

    @Override
    public List<MateLoadingVO> getSameMaterialAreas(String mcode, String fcode, String excludeAreaCd) {
        try {
            List<MateLoadingVO> sameMaterialAreas = mateLoadingMapper.getSameMaterialAreas(mcode, fcode, excludeAreaCd);
            System.out.println("동일 자재 적재 구역 조회 완료: " + mcode + " - " + sameMaterialAreas.size() + "개 구역");
            return sameMaterialAreas;
        } catch (Exception e) {
            System.err.println("동일 자재 적재 구역 조회 실패: " + mcode + " - " + e.getMessage());
            throw new RuntimeException("동일 자재 적재 구역 조회 실패: " + e.getMessage(), e);
        }
    }

    @Override
    public String generateWareStockCode() {
        try {
            // 현재 날짜 (yyMMdd 형식)
            String datePattern = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMdd"));
            
            // 해당 날짜의 마지막 순번 조회
            int lastSequence = mateLoadingMapper.getLastWareStockSequence(datePattern);
            
            // 다음 순번 계산 (1부터 시작)
            int nextSequence = lastSequence + 1;
            
            // WStock-yyMMdd-순번 형식으로 생성
            String wslCode = String.format("WStock-%s-%03d", datePattern, nextSequence);
            
            System.out.println("창고재고목록코드 생성: " + wslCode);
            return wslCode;
            
        } catch (Exception e) {
            System.err.println("창고재고목록코드 생성 실패: " + e.getMessage());
            
            // 실패 시 임시 코드 생성
            String datePattern = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMdd"));
            long timestamp = System.currentTimeMillis() % 1000;
            String fallbackCode = String.format("WStock-%s-%03d", datePattern, (int) timestamp);
            
            System.out.println("임시 창고재고목록코드 생성: " + fallbackCode);
            return fallbackCode;
        }
    }
    
    /**
     * MATERIAL 테이블의 mate_type을 WARE_STOCK 테이블의 item_type으로 변환
     * @param mateType MATERIAL 테이블의 mate_type (H1, H2 등)
     * @return WARE_STOCK 테이블의 item_type (h1, h2 등)
     */
    private String convertMateTypeToItemType(String mateType) {
        if (mateType == null || mateType.trim().isEmpty()) {
            System.out.println("mate_type이 null이거나 비어있음. 기본값 h1 사용");
            return "h1"; // 기본값: 원자재
        }
        
        String upperMateType = mateType.toUpperCase().trim();
        String itemType;
        
        switch (upperMateType) {
            case "H1":
                itemType = "h1"; // 원자재
                break;
            case "H2":
                itemType = "h2"; // 부자재
                break;
            default:
                System.out.println("알 수 없는 mate_type: " + mateType + ". 기본값 h1 사용");
                itemType = "h1"; // 기본값: 원자재
                break;
        }
        
        System.out.println("mate_type 변환: " + mateType + " -> " + itemType);
        return itemType;
    }
}
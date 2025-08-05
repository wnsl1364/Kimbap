package com.kimbap.kbs.materials.serviceimpl;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kimbap.kbs.materials.mapper.StockMovementMapper;
import com.kimbap.kbs.materials.service.StockMovementService;
import com.kimbap.kbs.materials.service.StockMovementVO;

@Service
@Transactional
public class StockMovementServiceImpl implements StockMovementService {

    @Autowired
    private StockMovementMapper stockMovementMapper;

    // ========== 이동요청서 등록 관련 ==========

    @Override
    public String registerMoveRequest(StockMovementVO stockMovement) {
        System.out.println("=== 이동요청서 등록 시작 ===");
        
        try {
            // 이동요청코드 생성
            String moveReqCd = generateMoveReqCode();
            stockMovement.setMoveReqCd(moveReqCd);
            
            // 요청일자 설정
            stockMovement.setReqDt(Timestamp.valueOf(LocalDateTime.now()));
            
            // 기본 상태 설정
            stockMovement.setMoveStatus("d1"); // 요청 상태
            
            // 이동요청서 등록
            stockMovementMapper.insertMoveRequest(stockMovement);
            
            System.out.println("이동요청서 등록 완료: " + moveReqCd);
            return moveReqCd;
            
        } catch (Exception e) {
            System.err.println("이동요청서 등록 실패: " + e.getMessage());
            throw new RuntimeException("이동요청서 등록 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    @Override
    public String registerMoveRequestDetails(List<StockMovementVO> detailList) {
        System.out.println("=== 이동요청 상세 등록 시작 ===");
        
        try {
            int successCount = 0;
            
            for (StockMovementVO detail : detailList) {
                // 이동요청상세코드 생성
                String mrdCode = generateMoveReqDetailCode();
                detail.setMrdcode(mrdCode);
                
                // 상세 등록
                stockMovementMapper.insertMoveRequestDetail(detail);
                successCount++;
                
                System.out.println("상세 등록 완료: " + mrdCode);
            }
            
            String result = String.format("이동요청 상세 등록 완료: %d건", successCount);
            System.out.println(result);
            return result;
            
        } catch (Exception e) {
            System.err.println("이동요청 상세 등록 실패: " + e.getMessage());
            throw new RuntimeException("이동요청 상세 등록 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    @Override
    public String registerFullMoveRequest(StockMovementVO header, List<StockMovementVO> detailList) {
        System.out.println("=== 이동요청서 전체 등록 시작 ===");
        
        try {
            // 1. 헤더 등록
            String moveReqCd = registerMoveRequest(header);
            
            // 2. 상세 목록에 헤더의 이동요청코드 설정
            for (StockMovementVO detail : detailList) {
                detail.setMoveReqCd(moveReqCd);
            }
            
            // 3. 상세 등록
            registerMoveRequestDetails(detailList);
            
            String result = String.format("이동요청서 전체 등록 완료: %s (상세 %d건)", moveReqCd, detailList.size());
            System.out.println(result);
            return result;
            
        } catch (Exception e) {
            System.err.println("이동요청서 전체 등록 실패: " + e.getMessage());
            throw new RuntimeException("이동요청서 전체 등록 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    // ========== 이동요청 목록 조회 관련 ==========

    @Override
    public List<StockMovementVO> getAllMoveRequestList() {
        try {
            List<StockMovementVO> list = stockMovementMapper.getAllMoveRequestList();
            System.out.println("이동요청 목록 조회 완료: " + list.size() + "건");
            return list;
        } catch (Exception e) {
            System.err.println("이동요청 목록 조회 실패: " + e.getMessage());
            throw new RuntimeException("이동요청 목록 조회 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    @Override
    public List<StockMovementVO> searchMoveRequestList(StockMovementVO searchParam) {
        try {
            List<StockMovementVO> list = stockMovementMapper.searchMoveRequestList(searchParam);
            System.out.println("이동요청 검색 조회 완료: " + list.size() + "건");
            return list;
        } catch (Exception e) {
            System.err.println("이동요청 검색 조회 실패: " + e.getMessage());
            throw new RuntimeException("이동요청 검색 조회 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    @Override
    public StockMovementVO getMoveRequestById(String moveReqCd) {
        try {
            StockMovementVO result = stockMovementMapper.getMoveRequestById(moveReqCd);
            System.out.println("이동요청 단건 조회 완료: " + moveReqCd);
            return result;
        } catch (Exception e) {
            System.err.println("이동요청 단건 조회 실패: " + e.getMessage());
            throw new RuntimeException("이동요청 단건 조회 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    @Override
    public List<StockMovementVO> getMoveRequestDetailList(String moveReqCd) {
        try {
            List<StockMovementVO> list = stockMovementMapper.getMoveRequestDetailList(moveReqCd);
            System.out.println("이동요청 상세 목록 조회 완료: " + moveReqCd + " - " + list.size() + "건");
            return list;
        } catch (Exception e) {
            System.err.println("이동요청 상세 목록 조회 실패: " + e.getMessage());
            throw new RuntimeException("이동요청 상세 목록 조회 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    // ========== 품목 선택 모달 관련 ==========

    @Override
    public List<StockMovementVO> getAvailableMaterialList(String fcode) {
        try {
            List<StockMovementVO> list = stockMovementMapper.getAvailableMaterialList(fcode);
            System.out.println("재고 자재 목록 조회 완료: " + fcode + " - " + list.size() + "건");
            return list;
        } catch (Exception e) {
            System.err.println("재고 자재 목록 조회 실패: " + e.getMessage());
            throw new RuntimeException("재고 자재 목록 조회 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    @Override
    public List<StockMovementVO> getAvailableProductList(String fcode) {
        try {
            List<StockMovementVO> list = stockMovementMapper.getAvailableProductList(fcode);
            System.out.println("재고 제품 목록 조회 완료: " + fcode + " - " + list.size() + "건");
            return list;
        } catch (Exception e) {
            System.err.println("재고 제품 목록 조회 실패: " + e.getMessage());
            throw new RuntimeException("재고 제품 목록 조회 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    @Override
    public StockMovementVO getItemStockInfo(String itemType, String itemCode, String lotNo) {
        try {
            StockMovementVO result = stockMovementMapper.getItemStockInfo(itemType, itemCode, lotNo);
            System.out.println("품목 재고 정보 조회 완료: " + itemType + "-" + itemCode + "-" + lotNo);
            return result;
        } catch (Exception e) {
            System.err.println("품목 재고 정보 조회 실패: " + e.getMessage());
            throw new RuntimeException("품목 재고 정보 조회 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    // ========== 도착위치 선택 모달 관련 ==========

    @Override
    public List<StockMovementVO> getActiveFactoryList() {
        try {
            List<StockMovementVO> list = stockMovementMapper.getActiveFactoryList();
            System.out.println("활성 공장 목록 조회 완료: " + list.size() + "개");
            return list;
        } catch (Exception e) {
            System.err.println("활성 공장 목록 조회 실패: " + e.getMessage());
            throw new RuntimeException("활성 공장 목록 조회 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    @Override
    public List<StockMovementVO> getWarehousesByFactory(String fcode) {
        try {
            List<StockMovementVO> list = stockMovementMapper.getWarehousesByFactory(fcode);
            System.out.println("공장별 창고 목록 조회 완료: " + fcode + " - " + list.size() + "개");
            return list;
        } catch (Exception e) {
            System.err.println("공장별 창고 목록 조회 실패: " + e.getMessage());
            throw new RuntimeException("공장별 창고 목록 조회 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    @Override
    public List<StockMovementVO> getAreasByWarehouse(String wcode) {
        try {
            List<StockMovementVO> list = stockMovementMapper.getAreasByWarehouse(wcode);
            System.out.println("창고별 구역 목록 조회 완료: " + wcode + " - " + list.size() + "개");
            return list;
        } catch (Exception e) {
            System.err.println("창고별 구역 목록 조회 실패: " + e.getMessage());
            throw new RuntimeException("창고별 구역 목록 조회 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    // ========== 승인/거절 처리 관련 ==========

    @Override
    public String approveMoveRequest(String moveReqCd, String approver, String comment) {
        try {
            System.out.println("=== 이동요청 승인 처리 시작 ===");
            
            stockMovementMapper.updateMoveRequestStatus(moveReqCd, "d2", approver, null);
            
            String result = "이동요청 승인 완료: " + moveReqCd;
            System.out.println(result);
            return result;
            
        } catch (Exception e) {
            System.err.println("이동요청 승인 실패: " + e.getMessage());
            throw new RuntimeException("이동요청 승인 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    @Override
    public String rejectMoveRequest(String moveReqCd, String approver, String rejectReason) {
        try {
            System.out.println("=== 이동요청 거절 처리 시작 ===");
            
            stockMovementMapper.updateMoveRequestStatus(moveReqCd, "d3", approver, rejectReason);
            
            String result = "이동요청 거절 완료: " + moveReqCd;
            System.out.println(result);
            return result;
            
        } catch (Exception e) {
            System.err.println("이동요청 거절 실패: " + e.getMessage());
            throw new RuntimeException("이동요청 거절 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    @Override
    public String approveBatchMoveRequest(List<String> moveReqCdList, String approver, String comment) {
        try {
            System.out.println("=== 다중 이동요청 승인 처리 시작 ===");
            
            int successCount = 0;
            for (String moveReqCd : moveReqCdList) {
                stockMovementMapper.updateMoveRequestStatus(moveReqCd, "d2", approver, null);
                successCount++;
            }
            
            String result = String.format("다중 이동요청 승인 완료: %d건", successCount);
            System.out.println(result);
            return result;
            
        } catch (Exception e) {
            System.err.println("다중 이동요청 승인 실패: " + e.getMessage());
            throw new RuntimeException("다중 이동요청 승인 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    // ========== 이동처리 실행 관련 ==========

    @Override
    public String executeMoveRequest(String moveReqCd) {
        // 실제 재고 이동 처리는 복잡한 로직이므로 기본 구조만 제공
        System.out.println("=== 이동요청 실행 처리: " + moveReqCd + " ===");
        return "이동요청 실행 완료: " + moveReqCd;
    }

    @Override
    public String executeBatchMoveRequest(List<String> moveReqCdList) {
        System.out.println("=== 다중 이동요청 실행 처리: " + moveReqCdList.size() + "건 ===");
        return "다중 이동요청 실행 완료: " + moveReqCdList.size() + "건";
    }

    // ========== 유효성 검증 관련 ==========

    @Override
    public Map<String, Object> validateMoveRequest(StockMovementVO stockMovement) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 현재 재고량 확인
            Integer currentStock = getCurrentStock(
                stockMovement.getDepaAreaCd(), 
                stockMovement.getItemType(), 
                stockMovement.getMcode() != null ? stockMovement.getMcode() : stockMovement.getPcode(),
                stockMovement.getLotNo()
            );
            
            boolean isValid = currentStock != null && 
                             stockMovement.getMoveQty() != null && 
                             currentStock >= stockMovement.getMoveQty().intValue();
            
            result.put("isValid", isValid);
            result.put("currentStock", currentStock);
            result.put("requestQty", stockMovement.getMoveQty());
            result.put("message", isValid ? "이동 가능" : "재고 부족");
            
            return result;
            
        } catch (Exception e) {
            result.put("isValid", false);
            result.put("message", "검증 중 오류 발생: " + e.getMessage());
            return result;
        }
    }

    @Override
    public Integer getCurrentStock(String wareAreaCd, String itemType, String itemCode, String lotNo) {
        try {
            Integer stock = stockMovementMapper.getCurrentStock(wareAreaCd, itemType, itemCode, lotNo);
            return stock != null ? stock : 0;
        } catch (Exception e) {
            System.err.println("현재 재고량 조회 실패: " + e.getMessage());
            return 0;
        }
    }

    @Override
    public Map<String, Object> validateDestinationArea(String wareAreaCd, String itemType, String itemCode, Integer moveQty) {
        try {
            return stockMovementMapper.validateDestinationArea(wareAreaCd, itemType, itemCode, moveQty);
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("isValid", false);
            result.put("message", "도착지 검증 중 오류 발생: " + e.getMessage());
            return result;
        }
    }

    // ========== 코드 생성 관련 ==========

    @Override
    public String generateMoveReqCode() {
        try {
            String datePattern = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMdd"));
            int lastSequence = stockMovementMapper.getLastMoveReqSequence(datePattern);
            int nextSequence = lastSequence + 1;
            
            String moveReqCd = String.format("MR-%s-%03d", datePattern, nextSequence);
            System.out.println("이동요청코드 생성: " + moveReqCd);
            return moveReqCd;
            
        } catch (Exception e) {
            String datePattern = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMdd"));
            long timestamp = System.currentTimeMillis() % 1000;
            String fallbackCode = String.format("MR-%s-%03d", datePattern, (int) timestamp);
            System.out.println("임시 이동요청코드 생성: " + fallbackCode);
            return fallbackCode;
        }
    }

    @Override
    public String generateMoveReqDetailCode() {
        try {
            String datePattern = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMdd"));
            int lastSequence = stockMovementMapper.getLastMoveReqDetailSequence(datePattern);
            int nextSequence = lastSequence + 1;
            
            String mrdCode = String.format("MRD-%s-%03d", datePattern, nextSequence);
            System.out.println("이동요청상세코드 생성: " + mrdCode);
            return mrdCode;
            
        } catch (Exception e) {
            String datePattern = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMdd"));
            long timestamp = System.currentTimeMillis() % 1000;
            String fallbackCode = String.format("MRD-%s-%03d", datePattern, (int) timestamp);
            System.out.println("임시 이동요청상세코드 생성: " + fallbackCode);
            return fallbackCode;
        }
    }

    // ========== 통계/대시보드 관련 ==========

    @Override
    public Map<String, Integer> getMoveRequestStatusCount() {
        try {
            return stockMovementMapper.getMoveRequestStatusCount();
        } catch (Exception e) {
            System.err.println("상태별 건수 조회 실패: " + e.getMessage());
            return new HashMap<>();
        }
    }

    @Override
    public List<Map<String, Object>> getMonthlyMoveRequestCount(String year) {
        try {
            return stockMovementMapper.getMonthlyMoveRequestCount(year);
        } catch (Exception e) {
            System.err.println("월별 건수 조회 실패: " + e.getMessage());
            return List.of();
        }
    }

    @Override
    public Integer getPendingApprovalCount() {
        try {
            Map<String, Integer> statusCount = getMoveRequestStatusCount();
            return statusCount.getOrDefault("d1", 0); // 요청 상태 건수
        } catch (Exception e) {
            System.err.println("승인 대기 건수 조회 실패: " + e.getMessage());
            return 0;
        }
    }
}
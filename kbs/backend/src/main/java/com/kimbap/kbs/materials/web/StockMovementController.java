package com.kimbap.kbs.materials.web;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kimbap.kbs.materials.service.StockMovementService;
import com.kimbap.kbs.materials.service.StockMovementVO;

@RestController
@RequestMapping("/api/materials/stockMovement")
@CrossOrigin(origins = "*")
public class StockMovementController {

    @Autowired
    private StockMovementService stockMovementService;

    // ========== 이동요청서 등록 관련 ==========

    // 이동요청서 등록 (헤더 + 상세 통합)
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> registerMoveRequest(@RequestBody Map<String, Object> requestData) {
        try {
            System.out.println("=== 이동요청서 등록 요청 ===");
            
            // 헤더 정보 추출
            Map<String, Object> headerMap = (Map<String, Object>) requestData.get("header");
            StockMovementVO header = convertMapToVO(headerMap);
            
            // 상세 정보 추출
            List<Map<String, Object>> detailMapList = (List<Map<String, Object>>) requestData.get("details");
            List<StockMovementVO> detailList = detailMapList.stream()
                .map(this::convertMapToVO)
                .toList();
            
            System.out.println("헤더 정보: " + header.toString());
            System.out.println("상세 건수: " + detailList.size());
            
            // 전체 등록 처리
            String result = stockMovementService.registerFullMoveRequest(header, detailList);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", result);
            response.put("moveReqCd", header.getMoveReqCd());
            response.put("detailCount", detailList.size());
            
            System.out.println("이동요청서 등록 완료: " + header.getMoveReqCd());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("이동요청서 등록 실패: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "이동요청서 등록 중 오류가 발생했습니다: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    // ========== 이동요청 목록 조회 관련 ==========

    // 이동요청 목록 전체 조회
    @GetMapping("/list")
    public ResponseEntity<List<StockMovementVO>> getAllMoveRequestList() {
        try {
            List<StockMovementVO> list = stockMovementService.getAllMoveRequestList();
            System.out.println("이동요청 목록 조회 완료: " + list.size() + "건");
            return ResponseEntity.ok(list);
        } catch (Exception e) {
            System.err.println("이동요청 목록 조회 실패: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    // 이동요청 목록 검색 조회
    @PostMapping("/search")
    public ResponseEntity<List<StockMovementVO>> searchMoveRequestList(@RequestBody StockMovementVO searchParam) {
        try {
            System.out.println("이동요청 검색 조회 요청: " + searchParam.toString());
            
            List<StockMovementVO> list = stockMovementService.searchMoveRequestList(searchParam);
            
            System.out.println("이동요청 검색 조회 완료: " + list.size() + "건");
            return ResponseEntity.ok(list);
        } catch (Exception e) {
            System.err.println("이동요청 검색 조회 실패: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    // 이동요청 단건 조회
    @GetMapping("/detail/{moveReqCd}")
    public ResponseEntity<StockMovementVO> getMoveRequestById(@PathVariable String moveReqCd) {
        try {
            System.out.println("이동요청 단건 조회 요청: " + moveReqCd);
            
            StockMovementVO result = stockMovementService.getMoveRequestById(moveReqCd);
            
            if (result != null) {
                System.out.println("이동요청 단건 조회 완료: " + moveReqCd);
                return ResponseEntity.ok(result);
            } else {
                System.out.println("해당 이동요청을 찾을 수 없음: " + moveReqCd);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            System.err.println("이동요청 단건 조회 실패: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    // 이동요청 상세 목록 조회
    @GetMapping("/details/{moveReqCd}")
    public ResponseEntity<List<StockMovementVO>> getMoveRequestDetailList(@PathVariable String moveReqCd) {
        try {
            System.out.println("이동요청 상세 목록 조회 요청: " + moveReqCd);
            
            List<StockMovementVO> list = stockMovementService.getMoveRequestDetailList(moveReqCd);
            
            System.out.println("이동요청 상세 목록 조회 완료: " + moveReqCd + " - " + list.size() + "건");
            return ResponseEntity.ok(list);
        } catch (Exception e) {
            System.err.println("이동요청 상세 목록 조회 실패: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    // ========== 품목 선택 모달 관련 ==========

    // 재고가 있는 자재 목록 조회
    @GetMapping("/available-materials")
    public ResponseEntity<List<StockMovementVO>> getAvailableMaterialList(@RequestParam String fcode) {
        try {
            System.out.println("재고 자재 목록 조회 요청: " + fcode);
            
            List<StockMovementVO> list = stockMovementService.getAvailableMaterialList(fcode);
            
            System.out.println("재고 자재 목록 조회 완료: " + fcode + " - " + list.size() + "건");
            return ResponseEntity.ok(list);
        } catch (Exception e) {
            System.err.println("재고 자재 목록 조회 실패: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    // 특정 품목의 재고 정보 조회
    @GetMapping("/item-stock")
    public ResponseEntity<StockMovementVO> getItemStockInfo(
            @RequestParam String itemType,
            @RequestParam String itemCode,
            @RequestParam String lotNo) {
        try {
            System.out.println("품목 재고 정보 조회 요청: " + itemType + "-" + itemCode + "-" + lotNo);
            
            StockMovementVO result = stockMovementService.getItemStockInfo(itemType, itemCode, lotNo);
            
            if (result != null) {
                System.out.println("품목 재고 정보 조회 완료");
                return ResponseEntity.ok(result);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            System.err.println("품목 재고 정보 조회 실패: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    // ========== 도착위치 선택 모달 관련 ==========

    // 활성화된 공장 목록 조회
    @GetMapping("/factories")
    public ResponseEntity<List<StockMovementVO>> getActiveFactoryList() {
        try {
            List<StockMovementVO> list = stockMovementService.getActiveFactoryList();
            System.out.println("활성 공장 목록 조회 완료: " + list.size() + "개");
            return ResponseEntity.ok(list);
        } catch (Exception e) {
            System.err.println("활성 공장 목록 조회 실패: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    // 특정 공장의 창고 목록 조회
    @GetMapping("/warehouses")
    public ResponseEntity<List<StockMovementVO>> getWarehousesByFactory(@RequestParam String fcode) {
        try {
            System.out.println("공장별 창고 목록 조회 요청: " + fcode);
            
            List<StockMovementVO> list = stockMovementService.getWarehousesByFactory(fcode);
            
            System.out.println("공장별 창고 목록 조회 완료: " + fcode + " - " + list.size() + "개");
            return ResponseEntity.ok(list);
        } catch (Exception e) {
            System.err.println("공장별 창고 목록 조회 실패: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    // 특정 창고의 구역 목록 조회
    @GetMapping("/areas")
    public ResponseEntity<List<StockMovementVO>> getAreasByWarehouse(@RequestParam String wcode) {
        try {
            System.out.println("창고별 구역 목록 조회 요청: " + wcode);
            
            List<StockMovementVO> list = stockMovementService.getAreasByWarehouse(wcode);
            
            System.out.println("창고별 구역 목록 조회 완료: " + wcode + " - " + list.size() + "개");
            return ResponseEntity.ok(list);
        } catch (Exception e) {
            System.err.println("창고별 구역 목록 조회 실패: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    // ========== 승인/거절 처리 관련 ==========

    // 이동요청 승인 처리
    @PutMapping("/approve/{moveReqCd}")
    public ResponseEntity<Map<String, Object>> approveMoveRequest(
            @PathVariable String moveReqCd,
            @RequestBody Map<String, String> requestData) {
        try {
            System.out.println("=== 이동요청 승인 처리 요청 ===");
            System.out.println("이동요청코드: " + moveReqCd);
            
            String approver = requestData.get("approver");
            String comment = requestData.get("comment");
            
            String result = stockMovementService.approveMoveRequest(moveReqCd, approver, comment);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", result);
            response.put("moveReqCd", moveReqCd);
            
            System.out.println("이동요청 승인 완료: " + moveReqCd);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("이동요청 승인 실패: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "이동요청 승인 중 오류가 발생했습니다: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    // 이동요청 거절 처리
    @PutMapping("/reject/{moveReqCd}")
    public ResponseEntity<Map<String, Object>> rejectMoveRequest(
            @PathVariable String moveReqCd,
            @RequestBody Map<String, String> requestData) {
        try {
            System.out.println("=== 이동요청 거절 처리 요청 ===");
            System.out.println("이동요청코드: " + moveReqCd);
            
            String approver = requestData.get("approver");
            String rejectReason = requestData.get("rejectReason");
            
            String result = stockMovementService.rejectMoveRequest(moveReqCd, approver, rejectReason);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", result);
            response.put("moveReqCd", moveReqCd);
            
            System.out.println("이동요청 거절 완료: " + moveReqCd);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("이동요청 거절 실패: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "이동요청 거절 중 오류가 발생했습니다: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    // 다중 이동요청 승인 처리
    @PutMapping("/approve-batch")
    public ResponseEntity<Map<String, Object>> approveBatchMoveRequest(@RequestBody Map<String, Object> requestData) {
        try {
            System.out.println("=== 다중 이동요청 승인 처리 요청 ===");
            
            List<String> moveReqCdList = (List<String>) requestData.get("moveReqCdList");
            String approver = (String) requestData.get("approver");
            String comment = (String) requestData.get("comment");
            
            System.out.println("처리 대상: " + moveReqCdList.size() + "건");
            
            String result = stockMovementService.approveBatchMoveRequest(moveReqCdList, approver, comment);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", result);
            response.put("processedCount", moveReqCdList.size());
            
            System.out.println("다중 이동요청 승인 완료: " + moveReqCdList.size() + "건");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("다중 이동요청 승인 실패: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "다중 이동요청 승인 중 오류가 발생했습니다: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    // ========== 유효성 검증 관련 ==========

    // 현재 재고량 조회
    @GetMapping("/current-stock")
    public ResponseEntity<Map<String, Object>> getCurrentStock(
            @RequestParam String wareAreaCd,
            @RequestParam String itemType,
            @RequestParam String itemCode,
            @RequestParam String lotNo) {
        try {
            Integer stock = stockMovementService.getCurrentStock(wareAreaCd, itemType, itemCode, lotNo);
            
            Map<String, Object> response = new HashMap<>();
            response.put("wareAreaCd", wareAreaCd);
            response.put("itemType", itemType);
            response.put("itemCode", itemCode);
            response.put("lotNo", lotNo);
            response.put("currentStock", stock);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("현재 재고량 조회 실패: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    // 이동 가능 여부 검증
    @PostMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateMoveRequest(@RequestBody StockMovementVO stockMovement) {
        try {
            System.out.println("이동 가능 여부 검증 요청");
            
            Map<String, Object> result = stockMovementService.validateMoveRequest(stockMovement);
            
            System.out.println("검증 결과: " + result.get("message"));
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            System.err.println("이동 가능 여부 검증 실패: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("isValid", false);
            errorResponse.put("message", "검증 중 오류가 발생했습니다: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    // ========== 통계/대시보드 관련 ==========

    // 이동요청 상태별 건수 조회
    @GetMapping("/status-count")
    public ResponseEntity<Map<String, Integer>> getMoveRequestStatusCount() {
        try {
            Map<String, Integer> result = stockMovementService.getMoveRequestStatusCount();
            System.out.println("상태별 건수 조회 완료");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            System.err.println("상태별 건수 조회 실패: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    // 승인 대기 건수 조회
    @GetMapping("/pending-count")
    public ResponseEntity<Map<String, Integer>> getPendingApprovalCount() {
        try {
            Integer count = stockMovementService.getPendingApprovalCount();
            
            Map<String, Integer> response = new HashMap<>();
            response.put("pendingCount", count);
            
            System.out.println("승인 대기 건수 조회 완료: " + count + "건");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("승인 대기 건수 조회 실패: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    // ========== 유틸리티 메서드 ==========

    // Map을 VO로 변환하는 헬퍼 메서드
    private StockMovementVO convertMapToVO(Map<String, Object> map) {
        StockMovementVO vo = new StockMovementVO();
        
        // 기본 필드들 매핑
        if (map.get("moveReqCd") != null) vo.setMoveReqCd((String) map.get("moveReqCd"));
        if (map.get("moveType") != null) vo.setMoveType((String) map.get("moveType"));
        if (map.get("moveRea") != null) vo.setMoveRea((String) map.get("moveRea"));
        if (map.get("requ") != null) vo.setRequ((String) map.get("requ"));
        if (map.get("note") != null) vo.setNote((String) map.get("note"));
        
        // 상세 필드들 매핑
        if (map.get("mcode") != null) vo.setMcode((String) map.get("mcode"));
        if (map.get("mateVerCd") != null) vo.setMateVerCd((String) map.get("mateVerCd"));
        if (map.get("pcode") != null) vo.setPcode((String) map.get("pcode"));
        if (map.get("prodVerCd") != null) vo.setProdVerCd((String) map.get("prodVerCd"));
        if (map.get("itemType") != null) vo.setItemType((String) map.get("itemType"));
        if (map.get("lotNo") != null) vo.setLotNo((String) map.get("lotNo"));
        if (map.get("moveQty") != null) vo.setMoveQty(new java.math.BigDecimal(map.get("moveQty").toString()));
        if (map.get("unit") != null) vo.setUnit((String) map.get("unit"));
        if (map.get("depaAreaCd") != null) vo.setDepaAreaCd((String) map.get("depaAreaCd"));
        if (map.get("arrAreaCd") != null) vo.setArrAreaCd((String) map.get("arrAreaCd"));
        if (map.get("depaWareCd") != null) vo.setDepaWareCd((String) map.get("depaWareCd"));
        if (map.get("arrWareCd") != null) vo.setArrWareCd((String) map.get("arrWareCd"));
        
        return vo;
    }
}
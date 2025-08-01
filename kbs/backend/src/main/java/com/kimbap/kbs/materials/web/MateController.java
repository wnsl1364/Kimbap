package com.kimbap.kbs.materials.web;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

import com.kimbap.kbs.materials.service.MateService;
import com.kimbap.kbs.materials.service.MaterialsVO;
import com.kimbap.kbs.materials.service.PurchaseOrderViewVO;
import com.kimbap.kbs.materials.service.SearchCriteria;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/materials")
@CrossOrigin(origins = "*")
public class MateController {

    @Autowired
    private MateService mateService;

    // ========== 자재입고 관련 API ==========
    /**
     * 자재입고 목록 조회
     */
    @GetMapping("/inbound")
    public ResponseEntity<List<MaterialsVO>> getMateInboList() {
        try {
            List<MaterialsVO> list = mateService.getMateInboList();
            return ResponseEntity.ok(list);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 자재입고 단건 조회
     */
    @GetMapping("/inbound/{mateInboCd}")
    public ResponseEntity<MaterialsVO> getMateInboById(@PathVariable String mateInboCd) {
        try {
            MaterialsVO mateInbo = mateService.getMateInboById(mateInboCd);
            if (mateInbo != null) {
                return ResponseEntity.ok(mateInbo);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 자재입고 등록
     */
    @PostMapping("/inbound")
    public ResponseEntity<String> insertMateInbo(@RequestBody MaterialsVO mateInbo) {
        try {
            mateService.insertMateInbo(mateInbo);
            return ResponseEntity.ok("자재입고가 성공적으로 등록되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("자재입고 등록 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 자재입고 수정 (상태 변경 등)
     */
    @PutMapping("/inbound")
    public ResponseEntity<String> updateMateInbo(@RequestBody MaterialsVO mateInbo) {
        try {
            mateService.updateMateInbo(mateInbo);
            return ResponseEntity.ok("자재입고 정보가 성공적으로 수정되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("자재입고 수정 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 공장목록 조회
     */
    @GetMapping("/factories")
    public ResponseEntity<List<MaterialsVO>> getActiveFactoryList() {
        try {
            List<MaterialsVO> factoryList = mateService.getActiveFactoryList();
            return ResponseEntity.ok(factoryList);
        } catch (Exception e) {
            System.err.println("공장 목록 조회 API 오류: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    // ========== 발주 관련 API (자재입고와 연관) ==========
    /**
     * 발주 목록 검색 및 조회 (자재입고 시 참조용)
     */
    @GetMapping("/purchaseOrders")
    public ResponseEntity<List<MaterialsVO>> getPurchaseOrders(
            @RequestParam(required = false) String purcCd,
            @RequestParam(required = false) String mateName,
            @RequestParam(required = false) String mcode,
            @RequestParam(required = false) String purcDStatus, // 발주상세상태
            @RequestParam(required = false) String purcStatus, // 발주상태 (추가!)
            @RequestParam(required = false) String cpCd, // 회사코드 (추가!)
            @RequestParam(required = false) String mateCpCd,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String exDeliStartDate,
            @RequestParam(required = false) String exDeliEndDate,
            @RequestParam(required = false) String deliStartDate,
            @RequestParam(required = false) String deliEndDate,
            @RequestParam(required = false) String memtype) {

        System.out.println("=== 파라미터 체크 ===");
        System.out.println("purcCd: [" + purcCd + "]");
        System.out.println("mateName: [" + mateName + "]");
        System.out.println("memtype: [" + memtype + "]");
        SearchCriteria criteria = SearchCriteria.builder()
                .purcCd(purcCd)
                .mateName(mateName)
                .mcode(mcode)
                .purcDStatus(purcDStatus)
                .purcStatus(purcStatus)
                .cpCd(cpCd)
                .startDate(startDate)
                .endDate(endDate)
                .exDeliStartDate(exDeliStartDate)
                .exDeliEndDate(exDeliEndDate)
                .deliStartDate(deliStartDate)
                .deliEndDate(deliEndDate)
                .memtype(memtype) // 권한에 따라 필터링
                .build();

        List<MaterialsVO> list = mateService.getPurchaseOrders(criteria);
        return ResponseEntity.ok(list);
    }

    // ========== 발주서 관련 새로운 API들 ==========
    /**
     * 발주서 목록 조회 (모달용) 사용처: 발주서 불러오기 모달에서 목록 표시
     */
    @GetMapping("/purchase-orders/list")
    public ResponseEntity<List<MaterialsVO>> getPurcOrderList() {
        try {
            List<MaterialsVO> list = mateService.getPurcOrderList();
            return ResponseEntity.ok(list);
        } catch (Exception e) {
            System.out.println("발주서 목록 조회 실패: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 발주서 상세 조회 (헤더 + 상세) 사용처: 발주서 불러오기 시 전체 데이터 가져오기
     */
    @GetMapping("/purchase-orders/{purcCd}")
    public ResponseEntity<Map<String, Object>> getPurcOrderWithDetails(@PathVariable String purcCd) {
        try {
            System.out.println("발주서 상세 조회 요청: " + purcCd);

            Map<String, Object> result = mateService.getPurcOrderWithDetails(purcCd);

            if (result.isEmpty()) {
                System.out.println("발주서를 찾을 수 없음: " + purcCd);
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            System.out.println("발주서 상세 조회 실패: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 자재-거래처 연결 조회 (검색용) 사용처: 자재 검색 모달, 거래처 검색 모달
     */
    @GetMapping("/materials-with-suppliers")
    public ResponseEntity<List<MaterialsVO>> getMaterialWithSuppliers(
            @RequestParam(required = false) String mcode,
            @RequestParam(required = false) String mateName,
            @RequestParam(required = false) String cpCd,
            @RequestParam(required = false) String cpName) {

        try {
            System.out.println("자재-거래처 검색 요청: mcode=" + mcode + ", mateName=" + mateName
                    + ", cpCd=" + cpCd + ", cpName=" + cpName);

            SearchCriteria criteria = SearchCriteria.builder()
                    .mcode(mcode)
                    .mateName(mateName)
                    .cpCd(cpCd)
                    .cpName(cpName)
                    .build();

            List<MaterialsVO> list = mateService.getMaterialWithSuppliers(criteria);
            System.out.println("자재-거래처 검색 결과: " + list.size() + "건");

            return ResponseEntity.ok(list);
        } catch (Exception e) {
            System.out.println("자재-거래처 검색 실패: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 발주서 저장 (신규 + 수정) 사용처: MaterialPurchase.vue에서 발주서 저장 시
     */
    @PostMapping("/purchase-orders")
    public ResponseEntity<Map<String, Object>> savePurchaseOrder(@RequestBody Map<String, Object> orderData) {
        try {
            System.out.println("발주서 저장 요청: " + orderData);

            // 필수 데이터 검증
            if (!orderData.containsKey("header") || !orderData.containsKey("details")) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "발주서 헤더 또는 상세 정보가 누락되었습니다.");

                return ResponseEntity.badRequest().body(errorResponse);
            }

            String purcCd = mateService.savePurchaseOrder(orderData);
            System.out.println("발주서 저장 완료: " + purcCd);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "발주서가 성공적으로 저장되었습니다.");
            response.put("purcCd", purcCd);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.out.println("발주서 저장 실패: " + e.getMessage());
            e.printStackTrace();

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "발주서 저장 중 오류가 발생했습니다: " + e.getMessage());

            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * 발주번호 자동생성 사용처: 신규 발주서 작성 시 발주번호 미리 생성
     */
    @PostMapping("/purchase-orders/generate-code")
    public ResponseEntity<Map<String, String>> generatePurchaseCode() {
        try {
            String purcCd = mateService.generatePurchaseCode();
            System.out.println("발주번호 자동생성: " + purcCd);

            Map<String, String> response = new HashMap<>();
            response.put("purcCd", purcCd);
            response.put("message", "발주번호가 생성되었습니다.");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.out.println("발주번호 생성 실패: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 자재 마스터 조회 (자재 검색용) 사용처: 자재 검색 모달
     */
    @GetMapping("/materials")
    public ResponseEntity<List<MaterialsVO>> getMaterials(
            @RequestParam(required = false) String mateName,
            @RequestParam(required = false) String mateType) {

        try {
            System.out.println("자재 마스터 조회: mateName=" + mateName + ", mateType=" + mateType);

            SearchCriteria criteria = SearchCriteria.builder()
                    .mateName(mateName)
                    .mateType(mateType)
                    .build();

            // 자재만 조회하는 로직 (거래처 정보 없이)
            List<MaterialsVO> list = mateService.getMaterialWithSuppliers(criteria);

            return ResponseEntity.ok(list);
        } catch (Exception e) {
            System.out.println("자재 마스터 조회 실패: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 거래처 마스터 조회 (거래처 검색용) 사용처: 거래처 검색 모달
     */
    @GetMapping("/suppliers")
    public ResponseEntity<List<MaterialsVO>> getSuppliers(
            @RequestParam(required = false) String cpName,
            @RequestParam(required = false) String cpType) {

        try {
            System.out.println("거래처 마스터 조회: cpName=" + cpName + ", cpType=" + cpType);

            SearchCriteria criteria = SearchCriteria.builder()
                    .cpName(cpName)
                    .cpCd(cpType) // 임시로 cpType을 cpCd에 매핑
                    .build();

            List<MaterialsVO> list = mateService.getMaterialWithSuppliers(criteria);

            return ResponseEntity.ok(list);
        } catch (Exception e) {
            System.out.println("거래처 마스터 조회 실패: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 🔥 특정 자재의 공급업체들 조회
     */
    @GetMapping("/materials/{mcode}/{mateVerCd}/suppliers")
    public ResponseEntity<List<MaterialsVO>> getSuppliersByMaterial(
            @PathVariable String mcode,
            @PathVariable String mateVerCd) {

        try {
            SearchCriteria criteria = SearchCriteria.builder()
                    .mcode(mcode)
                    .mateVerCd(mateVerCd)
                    .build();

            List<MaterialsVO> list = mateService.getSuppliersByMaterial(criteria);
            return ResponseEntity.ok(list);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 🔥 특정 거래처의 자재들 조회
     */
    @GetMapping("/suppliers/{cpCd}/materials")
    public ResponseEntity<List<MaterialsVO>> getMaterialsBySupplier(
            @PathVariable String cpCd) {

        try {
            SearchCriteria criteria = SearchCriteria.builder()
                    .cpCd(cpCd)
                    .build();

            List<MaterialsVO> list = mateService.getMaterialsBySupplier(criteria);
            return ResponseEntity.ok(list);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/purchase-orders/approval-list")
    public ResponseEntity<List<MaterialsVO>> getPurcOrderDetailListForApproval() {
        try {
            List<MaterialsVO> list = mateService.getPurcOrderDetailListForApproval();
            return ResponseEntity.ok(list);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/purchase-orders/status")
    public ResponseEntity<Map<String, Object>> updatePurchaseOrderStatus(
            @RequestBody MaterialsVO statusUpdateData) {
        try {
            System.out.println("🔄 발주 상태 업데이트 요청: " + statusUpdateData.getPurcDCd() 
                            + " → " + statusUpdateData.getPurcDStatus());

            // 필수 데이터 검증
            if (statusUpdateData.getPurcDCd() == null || statusUpdateData.getPurcDCd().trim().isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "발주상세코드가 필요합니다.");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            if (statusUpdateData.getPurcDStatus() == null || statusUpdateData.getPurcDStatus().trim().isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "변경할 상태가 필요합니다.");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            // 상태 업데이트 실행
            mateService.updatePurchaseOrderStatus(statusUpdateData);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "발주 상태가 성공적으로 업데이트되었습니다.");
            response.put("purcDCd", statusUpdateData.getPurcDCd());
            response.put("newStatus", statusUpdateData.getPurcDStatus());

            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("❌ 발주 상태 업데이트 실패: " + e.getMessage());
            e.printStackTrace();

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "발주 상태 업데이트 중 오류가 발생했습니다: " + e.getMessage());

            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * 📋 승인 대기 발주 목록 조회 (관리자용)
     */
    @GetMapping("/purchase-orders/pending-approval")
    public ResponseEntity<List<MaterialsVO>> getPendingApprovalOrders(
            @RequestParam(required = false) String purcCd,
            @RequestParam(required = false) String mateName,
            @RequestParam(required = false) String cpName,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String minAmount,
            @RequestParam(required = false) String maxAmount) {

        try {
            System.out.println("📋 승인 대기 발주 목록 조회 요청");

            SearchCriteria criteria = SearchCriteria.builder()
                    .purcCd(purcCd)
                    .mateName(mateName)
                    .cpName(cpName)
                    .purcDStatus("c1")  // 승인 대기 상태만 조회
                    .startDate(startDate)
                    .endDate(endDate)
                    .memtype("p1")      // 내부직원용
                    .build();

            List<MaterialsVO> list = mateService.getPurchaseOrders(criteria);
            
            // 승인 대기 상태만 필터링 (이중 체크)
            List<MaterialsVO> pendingList = list.stream()
                    .filter(item -> "c1".equals(item.getPurcDStatus()))
                    .collect(Collectors.toList());

            System.out.println("✅ 승인 대기 발주 조회 완료: " + pendingList.size() + "건");
            return ResponseEntity.ok(pendingList);
            
        } catch (Exception e) {
            System.err.println("❌ 승인 대기 발주 목록 조회 실패: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 📊 발주 통계 조회 (대시보드용)
     */
    @GetMapping("/purchase-orders/statistics")
    public ResponseEntity<Map<String, Object>> getPurchaseOrderStatistics(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {

        try {
            System.out.println("📊 발주 통계 조회 요청: " + startDate + " ~ " + endDate);

            SearchCriteria criteria = SearchCriteria.builder()
                    .startDate(startDate)
                    .endDate(endDate)
                    .memtype("p1")
                    .build();

            // 전체 발주 데이터 조회
            List<MaterialsVO> allOrders = mateService.getPurchaseOrders(criteria);

            // 상태별 통계 계산
            Map<String, Long> statusCounts = allOrders.stream()
                    .collect(Collectors.groupingBy(
                        MaterialsVO::getPurcDStatus,
                        Collectors.counting()
                    ));

            // 총 금액 계산
            BigDecimal totalAmount = allOrders.stream()
                    .filter(order -> order.getUnitPrice() != null && order.getPurcQty() != null)
                    .map(order -> order.getUnitPrice().multiply(BigDecimal.valueOf(order.getPurcQty())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // 공급업체별 통계
            Map<String, Long> supplierCounts = allOrders.stream()
                    .filter(order -> order.getCpName() != null)
                    .collect(Collectors.groupingBy(
                        MaterialsVO::getCpName,
                        Collectors.counting()
                    ));

            Map<String, Object> statistics = new HashMap<>();
            statistics.put("totalOrders", allOrders.size());
            statistics.put("pendingApproval", statusCounts.getOrDefault("c1", 0L));
            statistics.put("approved", statusCounts.getOrDefault("c2", 0L));
            statistics.put("rejected", statusCounts.getOrDefault("c6", 0L));
            statistics.put("totalAmount", totalAmount);
            statistics.put("statusBreakdown", statusCounts);
            statistics.put("topSuppliers", supplierCounts);

            System.out.println("✅ 발주 통계 조회 완료");
            return ResponseEntity.ok(statistics);
            
        } catch (Exception e) {
            System.err.println("❌ 발주 통계 조회 실패: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/purchase-orders-view")
    public ResponseEntity<List<PurchaseOrderViewVO>> getPurchaseOrdersForView(
            @RequestParam(required = false) String purcCd,
            @RequestParam(required = false) String purcDCd,
            @RequestParam(required = false) String mateName,
            @RequestParam(required = false) String mcode,
            @RequestParam(required = false) String purcDStatus,
            @RequestParam(required = false) String purcStatus,
            @RequestParam(required = false) String cpCd,
            @RequestParam(required = false) String cpName,
            @RequestParam(required = false) String mateType,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String exDeliStartDate,
            @RequestParam(required = false) String exDeliEndDate,
            @RequestParam(required = false) String memtype) {

        try {
            System.out.println("🎯 발주 조회 전용 API 호출: " + memtype);
            
            SearchCriteria criteria = SearchCriteria.builder()
                    .purcCd(purcCd)
                    .purcDCd(purcDCd)
                    .mateName(mateName)
                    .mcode(mcode)
                    .purcDStatus(purcDStatus)
                    .purcStatus(purcStatus)
                    .cpCd(cpCd)
                    .cpName(cpName)
                    .mateType(mateType)
                    .startDate(startDate)
                    .endDate(endDate)
                    .exDeliStartDate(exDeliStartDate)
                    .exDeliEndDate(exDeliEndDate)
                    .memtype(memtype)
                    .build();

            List<PurchaseOrderViewVO> list = mateService.getPurchaseOrdersForView(criteria);
            
            System.out.println("✅ 발주 조회 전용 API 성공: " + list.size() + "건");
            return ResponseEntity.ok(list);
            
        } catch (Exception e) {
            System.err.println("❌ 발주 조회 전용 API 실패: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 🔥 자재-거래처 연결 목록 조회
     */
    @GetMapping("/supplier-mate-relations")
    public ResponseEntity<List<PurchaseOrderViewVO>> getSupplierMateRelList(
            @RequestParam(required = false) String purcCd,
            @RequestParam(required = false) String mcode,
            @RequestParam(required = false) String mateType,
            @RequestParam(required = false) String exDeliStartDate,
            @RequestParam(required = false) String exDeliEndDate,
            @RequestParam(required = false) String deliStartDate,
            @RequestParam(required = false) String deliEndDate,
            HttpServletRequest request) {
        
        String loggedInCpCd = getCurrentUserCpCd(request);
        try {
            SearchCriteria criteria = SearchCriteria.builder()
                    .cpCd(loggedInCpCd)
                    .purcCd(purcCd)
                    .mcode(mcode)
                    .mateType(mateType)
                    .exDeliStartDate(exDeliStartDate)
                    .exDeliEndDate(exDeliEndDate)
                    .deliStartDate(deliStartDate)
                    .deliEndDate(deliEndDate)
                    .build();

            List<PurchaseOrderViewVO> list = mateService.getSupplierMateRelList(criteria);
            System.out.println("자재-거래처 연결 목록 조회 결과: " + list.size() + "건");

            return ResponseEntity.ok(list);
        } catch (Exception e) {
            System.out.println("자재-거래처 연결 목록 조회 실패: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    // getCurrentUserCpCd
    private String getCurrentUserCpCd(HttpServletRequest request) {
        // 현재 로그인한 사용자의 회사코드를 가져오는 로직
        // 예시로 세션에서 cpCd를 가져온다고 가정
        String cpCd = (String) request.getSession().getAttribute("cpCd");
        if (cpCd == null || cpCd.isEmpty()) {
            throw new IllegalStateException("로그인된 사용자의 회사코드가 없습니다.");
        }
        return cpCd;
    }
}

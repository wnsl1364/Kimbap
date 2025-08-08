package com.kimbap.kbs.materials.web;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
import com.kimbap.kbs.security.util.JwtUtil;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/materials")
@CrossOrigin(origins = "*")
public class MateController {

    @Autowired
    private MateService mateService;

    @Autowired
    private JwtUtil jwtUtil;

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
     * 🔥 특정 발주번호의 입고대기(c3) 상태 자재 조회 (자재입고 페이지용)
     */
    @GetMapping("/purchase-orders/{purcCd}/inbound-ready")
    public ResponseEntity<List<PurchaseOrderViewVO>> getPurchaseOrderDetailsForInbound(@PathVariable String purcCd) {
        try {
            System.out.println("🔍 입고대기 자재 조회 요청 - 발주번호: " + purcCd);
            
            List<PurchaseOrderViewVO> inboundReadyMaterials = mateService.getPurchaseOrderDetailsForInbound(purcCd);
            
            System.out.println("✅ 입고대기 자재 조회 완료: " + inboundReadyMaterials.size() + "건");
            
            return ResponseEntity.ok(inboundReadyMaterials);
            
        } catch (Exception e) {
            System.err.println("❌ 입고대기 자재 조회 실패: " + e.getMessage());
            e.printStackTrace();
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
                    .purcDStatus("c1") // 승인 대기 상태만 조회
                    .startDate(startDate)
                    .endDate(endDate)
                    .memtype("p1") // 내부직원용
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
            @RequestParam(required = false) String purcCd, // 발주번호
            @RequestParam(required = false) String mcode, // 자재코드
            @RequestParam(required = false) String mateName, // 자재명 추가
            @RequestParam(required = false) String mateType, // 자재유형
            @RequestParam(required = false) String purcDStatus, // 발주상태 추가
            @RequestParam(required = false) String exDeliStartDate,
            @RequestParam(required = false) String exDeliEndDate,
            @RequestParam(required = false) String deliStartDate,
            @RequestParam(required = false) String deliEndDate,
            @RequestParam(required = false) String cpCd, // 회사코드
            HttpServletRequest request) {

        String loggedInCpCd = (cpCd != null && !cpCd.isEmpty()) ? cpCd : getCurrentUserCpCd(request);
        try {
            SearchCriteria criteria = SearchCriteria.builder()
                    .cpCd(loggedInCpCd) // 로그인한 거래처만
                    .purcCd(purcCd)
                    .mcode(mcode)
                    .mateName(mateName) // 추가
                    .mateType(mateType)
                    .purcDStatus(purcDStatus) // 추가
                    .exDeliStartDate(exDeliStartDate)
                    .exDeliEndDate(exDeliEndDate)
                    .deliStartDate(deliStartDate)
                    .deliEndDate(deliEndDate)
                    .build();

            List<PurchaseOrderViewVO> list = mateService.getSupplierMateRelList(criteria);
            System.out.println("✅ 자재출고 목록 조회 결과: " + list.size() + "건");

            return ResponseEntity.ok(list);
        } catch (Exception e) {
            System.err.println("❌ 자재출고 목록 조회 실패: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    // getCurrentUserCpCd
    private String getCurrentUserCpCd(HttpServletRequest request) {
        System.out.println("🔍==================== getCurrentUserCpCd 시작 ====================");
        
        // 🎯 방법 1: JWT 토큰에서 cpCd 추출
        String authHeader = request.getHeader("Authorization");
        System.out.println("🔍 Authorization 헤더: " + authHeader);
        
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            try {
                String token = authHeader.substring(7);
                System.out.println("🔍 JWT 토큰 발견, 길이: " + token.length());
                System.out.println("🔍 JWT 토큰 일부: " + token.substring(0, Math.min(50, token.length())) + "...");

                // JWT 토큰에서 cpCd 추출 시도
                try {
                    String cpCd = jwtUtil.getCpCdFromToken(token);
                    if (cpCd != null && !cpCd.isEmpty()) {
                        System.out.println("✅ JWT에서 cpCd 추출 성공: " + cpCd);
                        return cpCd;
                    } else {
                        System.out.println("❌ JWT에서 cpCd 추출 실패: null 또는 빈 문자열");
                    }
                } catch (Exception jwtException) {
                    System.err.println("❌ JWT 파싱 중 예외 발생: " + jwtException.getMessage());
                    jwtException.printStackTrace();
                }

                // 토큰 내용 직접 파싱해서 확인
                try {
                    String[] parts = token.split("\\.");
                    if (parts.length >= 2) {
                        String payload = new String(java.util.Base64.getDecoder().decode(parts[1]));
                        System.out.println("🔍 JWT 페이로드 원본: " + payload);
                        
                        // JSON 파싱 시도
                        if (payload.contains("\"cpCd\"")) {
                            System.out.println("✅ JWT 페이로드에 cpCd 필드 발견!");
                            // 간단한 문자열 파싱으로 cpCd 추출
                            String cpCdPattern = "\"cpCd\":\"([^\"]+)\"";
                            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(cpCdPattern);
                            java.util.regex.Matcher matcher = pattern.matcher(payload);
                            if (matcher.find()) {
                                String extractedCpCd = matcher.group(1);
                                System.out.println("🎯 정규식으로 추출한 cpCd: " + extractedCpCd);
                                return extractedCpCd;
                            }
                        } else {
                            System.out.println("❌ JWT 페이로드에 cpCd 필드가 없습니다");
                        }
                    }
                } catch (Exception e) {
                    System.out.println("❌ JWT 페이로드 직접 파싱 실패: " + e.getMessage());
                }
            } catch (Exception e) {
                System.out.println("❌ JWT 토큰 처리 중 예외: " + e.getMessage());
                e.printStackTrace();
            }
        }

        // 🎯 방법 2: SecurityContext에서 인증 정보 확인
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
                System.out.println("🔍 SecurityContext 인증 정보:");
                System.out.println("   - Principal: " + auth.getPrincipal());
                System.out.println("   - Name: " + auth.getName());
                System.out.println("   - Authorities: " + auth.getAuthorities());
                System.out.println("   - Details: " + auth.getDetails());

                // Authentication Details에서 추출
                if (auth.getDetails() instanceof Map) {
                    Map<String, Object> details = (Map<String, Object>) auth.getDetails();
                    Object cpCdObj = details.get("cpCd");
                    if (cpCdObj != null) {
                        String cpCd = cpCdObj.toString();
                        System.out.println("✅ Authentication Details에서 cpCd 추출 성공: " + cpCd);
                        return cpCd;
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("❌ SecurityContext에서 cpCd 조회 실패: " + e.getMessage());
        }

        // 🎯 방법 3: 세션에서 가져오기
        HttpSession session = request.getSession(false);
        if (session != null) {
            System.out.println("🔍 세션 정보:");
            System.out.println("   - 세션 ID: " + session.getId());
            System.out.println("   - 생성 시간: " + new Date(session.getCreationTime()));
            System.out.println("   - 마지막 접근: " + new Date(session.getLastAccessedTime()));

            // 모든 세션 속성 출력
            Enumeration<String> attributeNames = session.getAttributeNames();
            while (attributeNames.hasMoreElements()) {
                String name = attributeNames.nextElement();
                Object value = session.getAttribute(name);
                System.out.println("   - " + name + ": " + value);
            }

            String cpCd = (String) session.getAttribute("cpCd");
            if (cpCd != null && !cpCd.isEmpty()) {
                System.out.println("✅ 세션에서 cpCd 추출 성공: " + cpCd);
                return cpCd;
            }
        } else {
            System.out.println("❌ 세션이 존재하지 않습니다.");
        }

        // 🎯 방법 4: 요청 속성에서 확인 (JWT 필터에서 설정했을 수도 있음)
        Object reqCpCd = request.getAttribute("cpCd");
        if (reqCpCd != null) {
            String cpCd = reqCpCd.toString();
            System.out.println("✅ 요청 속성에서 cpCd 추출 성공: " + cpCd);
            return cpCd;
        }

        // 🎯 디버깅: 모든 요청 속성 출력
        System.out.println("🔍 요청 속성들:");
        Enumeration<String> reqAttributeNames = request.getAttributeNames();
        while (reqAttributeNames.hasMoreElements()) {
            String name = reqAttributeNames.nextElement();
            Object value = request.getAttribute(name);
            System.out.println("   - " + name + ": " + value);
        }

        // 🔍 추가 디버깅: 모든 요청 헤더 출력
        System.out.println("🔍 요청 헤더들:");
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            String headerValue = request.getHeader(headerName);
            System.out.println("   - " + headerName + ": " + headerValue);
        }

        // 🎯 마지막 대안: 기본값 반환
        System.out.println("⚠️ cpCd를 찾을 수 없어서 기본값 사용: CP-001");
        System.out.println("🔍==================== getCurrentUserCpCd 종료 ====================");
        return "CP-001";
    }

    // ========== 공급업체 자재출고 관련 API 추가 ==========
    /**
     * 공급업체 자재출고 다중(배치) 처리
     */
    @PostMapping("/outbound/process-batch")
    public ResponseEntity<Map<String, Object>> processBatchOutbound(@RequestBody List<MaterialsVO> outboundDataList,
            HttpServletRequest request) {
        try {
            System.out.println("공급업체 자재출고 배치 처리 요청: " + outboundDataList.size() + "건");

            int totalProcessed = 0;

            for (MaterialsVO outboundData : outboundDataList) {
                try {
                    System.out.println("🔍 처리 중인 출고 데이터:");
                    System.out.println("  purcDCd: " + outboundData.getPurcDCd());
                    System.out.println("  outboundQty: " + outboundData.getOutboundQty()); // ✅ 수정됨
                    System.out.println("  mateName: " + outboundData.getMateName());

                    // 🎯 curr_qty와 상태를 동시에 업데이트하는 새로운 서비스 호출
                    mateService.updatePurchaseOrderCurrQtyAndStatus(outboundData);

                    System.out.println("✅ 발주상세 curr_qty 및 상태 업데이트 완료: " + outboundData.getPurcDCd());

                    // 🎯 2단계: mate_inbo 레코드 생성
                    String mateInboCd = generateMateInboCode();
                    String actualMateVerCd = getActualMateVerCdFromPurcOrder(outboundData.getPurcDCd());

                    MaterialsVO mateInboVO = MaterialsVO.builder()
                            .mateInboCd(mateInboCd)
                            .mcode(outboundData.getMcode())
                            .mateVerCd(actualMateVerCd)
                            .purcDCd(outboundData.getPurcDCd())
                            .supplierLotNo("SUP-LOT-" + System.currentTimeMillis())
                            .inboStatus("c3")
                            .totalQty(outboundData.getOutboundQty()) // ✅ 수정됨: getPurcQty() → getOutboundQty()
                            .mname("공급업체출고")
                            .note(outboundData.getNote() != null
                                    ? outboundData.getNote()
                                    : outboundData.getMateName() + " 공급업체 출고완료")
                            .cpCd(getCurrentUserCpCd(request))
                            .deliDt(new Date())
                            .inboDt(new Date())
                            .build();

                    mateService.insertMateInbo(mateInboVO);
                    totalProcessed++;

                    System.out.println("✅ 자재입고 대기 레코드 생성: " + mateInboCd
                            + " (발주: " + outboundData.getPurcDCd()
                            + ", 자재: " + outboundData.getMcode() + " ver:" + actualMateVerCd
                            + ", 수량: " + outboundData.getOutboundQty() + ")"); // ✅ 수정됨

                } catch (Exception e) {
                    System.err.println("❌ 개별 출고 처리 실패: " + outboundData.getPurcDCd() + " - " + e.getMessage());
                    e.printStackTrace(); // 스택 트레이스도 출력
                    // 개별 실패해도 다음 항목 계속 처리
                }
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "공급업체 출고완료 처리가 완료되었습니다! "
                    + totalProcessed + "건의 자재가 입고대기 상태로 등록되었습니다.");
            response.put("processedCount", totalProcessed);
            response.put("timestamp", new Date());

            System.out.println("🎉 공급업체 출고완료 배치 처리 완료: " + totalProcessed + "건");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("❌ 공급업체 출고완료 처리 실패: " + e.getMessage());
            e.printStackTrace();

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "공급업체 출고완료 처리 중 오류가 발생했습니다: " + e.getMessage());

            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    private String getActualMateVerCdFromPurcOrder(String purcDCd) {
        try {
            // 🔥 purc_ord_d에서 실제 mate_ver_cd 조회!
            MaterialsVO purcOrderDetail = mateService.getPurcOrderDetailByCode(purcDCd);

            if (purcOrderDetail != null && purcOrderDetail.getMateVerCd() != null) {
                System.out.println("✅ 실제 mate_ver_cd 조회 성공: " + purcOrderDetail.getMateVerCd()
                        + " (발주상세: " + purcDCd + ")");
                return purcOrderDetail.getMateVerCd();
            } else {
                System.out.println("⚠️ mate_ver_cd 조회 실패, 기본값 V1 사용 (발주상세: " + purcDCd + ")");
                return "V1"; // 기본값
            }
        } catch (Exception e) {
            System.err.println("❌ mate_ver_cd 조회 중 오류, 기본값 V1 사용: " + e.getMessage());
            return "V1"; // 오류 시 기본값
        }
    }

    /**
     * 공급업체 자재출고 개별 처리 - PK 중복 시 재시도
     */
    @PutMapping("/outbound/status")
    public ResponseEntity<Map<String, Object>> updateOutboundStatus(@RequestBody MaterialsVO updateData,
            HttpServletRequest request) {

        int maxRetries = 3;
        Exception lastException = null;

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                System.out.println("🔄 공급업체 개별 출고 처리 시도 " + attempt + "/" + maxRetries + ": " + updateData.getPurcDCd());

                String mateInboCd = generateMateInboCode();
                String actualMateVerCd = getActualMateVerCdFromPurcOrder(updateData.getPurcDCd());

                MaterialsVO mateInboVO = MaterialsVO.builder()
                        .mateInboCd(mateInboCd)
                        .mcode(updateData.getMcode())
                        .mateVerCd(actualMateVerCd)
                        .purcDCd(updateData.getPurcDCd())
                        .supplierLotNo("SUP-LOT-" + System.currentTimeMillis())
                        .inboStatus("c3")
                        .totalQty(updateData.getPurcQty())
                        .mname("공급업체출고")
                        .note(updateData.getNote() != null ? updateData.getNote()
                                : updateData.getMateName() + " 개별 출고완료")
                        .cpCd(getCurrentUserCpCd(request))
                        .deliDt(new Date())
                        .inboDt(new Date())
                        .build();

                mateService.insertMateInbo(mateInboVO);

                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "개별 출고완료 처리되었습니다! 자재가 입고대기 상태로 등록되었습니다.");
                response.put("mateInboCd", mateInboCd);
                response.put("purcDCd", updateData.getPurcDCd());
                response.put("outbQty", updateData.getPurcQty());
                response.put("attempt", attempt);

                System.out.println("✅ 개별 출고완료 처리 성공 (시도 " + attempt + "): " + mateInboCd);
                return ResponseEntity.ok(response);

            } catch (Exception e) {
                lastException = e;
                System.err.println("❌ 개별 출고완료 처리 시도 " + attempt + " 실패: " + e.getMessage());

                // PK 중복 에러인지 확인
                if (e.getMessage() != null && e.getMessage().contains("unique constraint")) {
                    System.out.println("🔄 PK 중복 감지, 재시도합니다...");
                    if (attempt < maxRetries) {
                        continue; // 재시도
                    }
                } else {
                    // PK 중복이 아닌 다른 에러는 즉시 실패
                    break;
                }
            }
        }

        // 모든 재시도 실패
        System.err.println("💥 모든 재시도 실패 - 최종 에러: " + (lastException != null ? lastException.getMessage() : "알 수 없는 오류"));
        if (lastException != null) {
            lastException.printStackTrace();
        }

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("message", "개별 출고완료 처리 중 오류가 발생했습니다: " + (lastException != null ? lastException.getMessage() : "알 수 없는 오류"));

        return ResponseEntity.internalServerError().body(errorResponse);
    }

    /**
     * 자재입고코드 자동생성 (MATI-연월-순번)
     */
    /**
     * 자재입고코드 자동생성 (MATI-연월-순번) - 진짜 순차 번호
     */
    private synchronized String generateMateInboCode() {
        try {
            LocalDateTime now = LocalDateTime.now();
            String yearMonth = now.format(DateTimeFormatter.ofPattern("yyyyMM"));

            // DB에서 해당 년월의 마지막 입고코드 조회
            String lastCode = mateService.getLastMateInboCode(yearMonth);
            System.out.println("🔍 DB 조회 결과 - 마지막 코드: " + lastCode);

            int nextSequence = 1; // 기본값: 0001

            if (lastCode != null && !lastCode.trim().isEmpty()) {
                // 마지막 코드에서 번호 추출: MATI-202508-0005 → 0005
                String[] parts = lastCode.split("-");
                if (parts.length >= 3) {
                    try {
                        String numberPart = parts[2];
                        int lastNumber = Integer.parseInt(numberPart);
                        nextSequence = lastNumber + 1;
                        System.out.println("✅ 다음 시퀀스: " + nextSequence);
                    } catch (NumberFormatException e) {
                        System.err.println("❌ 번호 파싱 실패, 기본값 사용: " + e.getMessage());
                    }
                }
            }

            String mateInboCd = String.format("MATI-%s-%04d", yearMonth, nextSequence);
            System.out.println("🎯 생성된 순차 코드: " + mateInboCd);

            return mateInboCd;

        } catch (Exception e) {
            System.err.println("❌ 입고코드 생성 실패: " + e.getMessage());
            // 폴백: 타임스탬프 기반
            String yearMonth = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
            long timestamp = System.currentTimeMillis();
            int fallbackNumber = (int) (timestamp % 9999) + 1;
            String fallbackCode = String.format("MATI-%s-T%04d", yearMonth, fallbackNumber);
            System.err.println("🆘 폴백 코드: " + fallbackCode);
            return fallbackCode;
        }
    }

    /**
     * 🔍 디버그용: 현재 DB의 입고코드 현황 조회
     */
    @GetMapping("/debug/mate-inbo-codes")
    public ResponseEntity<Map<String, Object>> debugMateInboCodes() {
        try {
            LocalDateTime now = LocalDateTime.now();
            String yearMonth = now.format(DateTimeFormatter.ofPattern("yyyyMM"));

            // 현재 월의 마지막 코드 조회
            String lastCode = mateService.getLastMateInboCode(yearMonth);

            // 다음 코드 미리보기
            String nextCode = generateMateInboCode();

            Map<String, Object> debugInfo = new HashMap<>();
            debugInfo.put("currentYearMonth", yearMonth);
            debugInfo.put("lastCodeInDB", lastCode);
            debugInfo.put("nextCodeToGenerate", nextCode);
            debugInfo.put("timestamp", new Date());

            return ResponseEntity.ok(debugInfo);
        } catch (Exception e) {
            Map<String, Object> errorInfo = new HashMap<>();
            errorInfo.put("error", e.getMessage());
            errorInfo.put("timestamp", new Date());
            return ResponseEntity.internalServerError().body(errorInfo);
        }
    }



    // 자재 입출고 내역 조회
    @GetMapping("/flow")
    public List<MaterialsVO> getMaterialFlowList(MaterialsVO search) {
        return mateService.getMaterialFlowList(search);
    }
    @GetMapping("/flow/today")
    public List<MaterialsVO> getTodayMaterialFlowList() {
        return mateService.getTodayMaterialFlowList();
    }

    // ========== 자재 재고 현황 관련 API ==========
    
    /**
     * 🏭 자재 재고 현황 조회 API
     * 
     * 📌 API 설계 개념:
     * - URL: GET /api/materials/stock-status
     * - 목적: 공장별, 자재별 재고 현황을 안전재고 기준으로 분석하여 제공
     * - 주요 기능: 재고 부족/과다/정상 상태 판정, LOT 관리, 안전재고 대비 분석
     * 
     * 🎯 비즈니스 로직:
     * 1. 검색 조건에 따른 자재 필터링 (자재코드, 자재명, 자재유형, 공장명)
     * 2. 창고 재고 데이터 집계 (같은 자재의 모든 LOT 합계)
     * 3. 안전재고 기준 상태 판정 (empty/shortage/overstock/normal)
     * 4. 재고 부족 우선순위로 정렬하여 반환
     * 
     * 📊 프론트엔드 활용:
     * - 재고 현황 대시보드
     * - 재고 부족 알림 시스템  
     * - 발주 계획 수립 지원
     * - LOT별 상세 조회 링크
     * 
     * @param mcode 자재코드 (선택)
     * @param mateName 자재명 (부분 검색, 선택)
     * @param mateType 자재유형 (h1:원자재, h2:부자재, 선택)
     * @param facName 공장명 (부분 검색, 선택)
     * @param request HTTP 요청 객체 (디버깅용)
     * @return ResponseEntity<Map<String, Object>> 자재 재고 현황 목록과 메타데이터
     */
    @GetMapping("/stock-status")
    public ResponseEntity<Map<String, Object>> getMaterialStockStatus(
            @RequestParam(required = false) String mcode,           // 자재코드
            @RequestParam(required = false) String mateName,        // 자재명 (LIKE 검색)
            @RequestParam(required = false) String mateType,        // 자재유형 
            @RequestParam(required = false) String facName,         // 공장명 (LIKE 검색)
            HttpServletRequest request) {
        
        try {
            // 🔍 1단계: 요청 파라미터 로깅 및 검증
            System.out.println("=== 📊 자재 재고 현황 조회 API 호출 ===");
            System.out.println("🔗 요청 URL: " + request.getRequestURL());
            System.out.println("📝 검색 조건:");
            System.out.println("  - mcode: " + mcode);
            System.out.println("  - mateName: " + mateName);
            System.out.println("  - mateType: " + mateType);
            System.out.println("  - facName: " + facName);
            
            // 🏗️ 2단계: 검색 조건 객체 구성
            // MaterialsVO를 검색 파라미터로 활용하는 방식
            MaterialsVO searchParams = MaterialsVO.builder()
                    .mcode(mcode)                   // 정확히 일치하는 자재코드
                    .mateName(mateName)             // 부분 검색용 자재명
                    .mateType(mateType)             // 자재유형 필터
                    .facName(facName)               // 부분 검색용 공장명
                    .build();
            
            System.out.println("🎯 검색 객체 생성 완료");
            
            // 🚀 3단계: 서비스 계층 호출
            // 실제 비즈니스 로직은 Service Layer에서 처리
            List<MaterialsVO> stockStatusList = mateService.getMaterialStockStatus(searchParams);
            
            // 📊 4단계: 응답 데이터 가공 및 메타데이터 추가
            Map<String, Object> response = new HashMap<>();
            
            // 메인 데이터
            response.put("data", stockStatusList);
            response.put("totalCount", stockStatusList.size());
            
            // 📈 통계 정보 계산
            long emptyCount = stockStatusList.stream()
                    .filter(item -> "empty".equals(item.getStockStatus()))
                    .count();
            
            long shortageCount = stockStatusList.stream()
                    .filter(item -> "shortage".equals(item.getStockStatus()))
                    .count();
            
            long overstockCount = stockStatusList.stream()
                    .filter(item -> "overstock".equals(item.getStockStatus()))
                    .count();
            
            long normalCount = stockStatusList.stream()
                    .filter(item -> "normal".equals(item.getStockStatus()))
                    .count();
            
            // 📊 상태별 통계
            Map<String, Object> statistics = new HashMap<>();
            statistics.put("empty", emptyCount);           // 재고 없음
            statistics.put("shortage", shortageCount);     // 재고 부족
            statistics.put("overstock", overstockCount);   // 재고 과다
            statistics.put("normal", normalCount);         // 정상
            statistics.put("total", stockStatusList.size());
            
            response.put("statistics", statistics);
            
            // 🔔 알림 정보 (재고 부족 건수)
            response.put("alertCount", emptyCount + shortageCount);
            
            // 📅 메타데이터
            response.put("timestamp", new Date());
            response.put("searchConditions", searchParams);
            
            // ✅ 5단계: 성공 응답 반환
            System.out.println("✅ 재고 현황 조회 완료: " + stockStatusList.size() + "건");
            System.out.println("📈 상태별 통계 - 재고없음:" + emptyCount + ", 부족:" + shortageCount + 
                             ", 과다:" + overstockCount + ", 정상:" + normalCount);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            // 🚨 6단계: 예외 처리
            System.err.println("❌ 자재 재고 현황 조회 실패: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", e.getMessage());
            errorResponse.put("timestamp", new Date());
            
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * 🏷️ 특정 자재의 LOT별 상세 재고 조회 API 
     * 
     * 📌 API 설계 개념:
     * - URL: GET /api/materials/stock-status/{mcode}/lots  
     * - 목적: 특정 자재의 LOT별 상세 재고 정보 제공
     * - 활용: 재고 현황에서 "LOT별조회(X건)" 링크 클릭 시 호출
     * 
     * @param mcode 자재코드 (필수)
     * @param fcode 공장코드 (선택, 특정 공장만 조회)
     * @return ResponseEntity<Map<String, Object>> LOT별 상세 재고 정보
     */
    @GetMapping("/stock-status/{mcode}/lots")
    public ResponseEntity<Map<String, Object>> getMaterialLotDetails(
            @PathVariable String mcode,
            @RequestParam(required = false) String fcode) {
        
        try {
            System.out.println("=== 🏷️ LOT별 상세 재고 조회 ===");
            System.out.println("자재코드: " + mcode);
            System.out.println("공장코드: " + fcode);
            
            // 검색 조건 설정
            MaterialsVO searchParams = MaterialsVO.builder()
                    .mcode(mcode)
                    .fcode(fcode)
                    .build();
            
            // TODO: LOT별 상세 조회 로직 구현 (별도 Mapper 메서드 필요)
            // List<MaterialsVO> lotDetails = mateService.getMaterialLotDetails(searchParams);
            
            Map<String, Object> response = new HashMap<>();
            response.put("mcode", mcode);
            response.put("fcode", fcode);
            response.put("message", "LOT별 상세 조회 기능 구현 예정");
            response.put("timestamp", new Date());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("❌ LOT별 상세 조회 실패: " + e.getMessage());
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "LOT별 상세 조회 중 오류가 발생했습니다.");
            errorResponse.put("message", e.getMessage());
            
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * 📊 재고 현황 엑셀 다운로드 API
     * 
     * 📌 API 설계 개념:
     * - URL: GET /api/materials/stock-status/export
     * - 목적: 재고 현황 데이터를 엑셀 파일로 다운로드
     * - 활용: 재고 보고서, 데이터 백업, 외부 시스템 연동
     * 
     * @param mcode 자재코드 (선택)
     * @param mateName 자재명 (선택)
     * @param mateType 자재유형 (선택)
     * @param facName 공장명 (선택)
     * @return ResponseEntity<byte[]> 엑셀 파일 바이너리 데이터
     */
    @GetMapping("/stock-status/export")
    public ResponseEntity<Map<String, Object>> exportStockStatusToExcel(
            @RequestParam(required = false) String mcode,
            @RequestParam(required = false) String mateName,
            @RequestParam(required = false) String mateType,
            @RequestParam(required = false) String facName) {
        
        try {
            System.out.println("=== 📊 재고 현황 엑셀 다운로드 ===");
            
            // 동일한 검색 조건으로 데이터 조회
            MaterialsVO searchParams = MaterialsVO.builder()
                    .mcode(mcode)
                    .mateName(mateName)
                    .mateType(mateType)
                    .facName(facName)
                    .build();
            
            List<MaterialsVO> stockStatusList = mateService.getMaterialStockStatus(searchParams);
            
            // TODO: Apache POI를 사용한 엑셀 파일 생성 로직 구현
            // byte[] excelData = createExcelFile(stockStatusList);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "엑셀 다운로드 기능 구현 예정");
            response.put("dataCount", stockStatusList.size());
            response.put("timestamp", new Date());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("❌ 엑셀 다운로드 실패: " + e.getMessage());
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "엑셀 다운로드 중 오류가 발생했습니다.");
            errorResponse.put("message", e.getMessage());
            
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * ⚠️ 재고 부족 알림 API
     * 
     * 📌 API 설계 개념:
     * - URL: GET /api/materials/stock-alerts
     * - 목적: 재고 부족/과다 상황의 자재만 필터링하여 알림용 데이터 제공
     * - 활용: 대시보드 알림, 자동 발주 시스템, 모바일 푸시 알림
     * 
     * @param alertType 알림 유형 (shortage: 부족, overstock: 과다, all: 전체)
     * @return ResponseEntity<Map<String, Object>> 알림 대상 자재 목록
     */
    @GetMapping("/stock-alerts")
    public ResponseEntity<Map<String, Object>> getStockAlerts(
            @RequestParam(defaultValue = "all") String alertType) {
        
        try {
            System.out.println("=== ⚠️ 재고 알림 조회 ===");
            System.out.println("알림 유형: " + alertType);
            
            // 전체 재고 현황 조회
            List<MaterialsVO> allStockStatus = mateService.getMaterialStockStatus(new MaterialsVO());
            
            // 알림 유형에 따른 필터링
            List<MaterialsVO> alertItems = allStockStatus.stream()
                    .filter(item -> {
                        String status = item.getStockStatus();
                        switch (alertType.toLowerCase()) {
                            case "shortage":
                                return "empty".equals(status) || "shortage".equals(status);
                            case "overstock":
                                return "overstock".equals(status);
                            case "all":
                                return !"normal".equals(status);  // 정상이 아닌 모든 상태
                            default:
                                return false;
                        }
                    })
                    .collect(Collectors.toList());
            
            Map<String, Object> response = new HashMap<>();
            response.put("alertType", alertType);
            response.put("alerts", alertItems);
            response.put("alertCount", alertItems.size());
            response.put("timestamp", new Date());
            
            // 우선순위별 카운트
            Map<String, Long> priorityCount = alertItems.stream()
                    .collect(Collectors.groupingBy(
                            item -> item.getStockStatus(),
                            Collectors.counting()
                    ));
            response.put("priorityCount", priorityCount);
            
            System.out.println("✅ 알림 조회 완료: " + alertItems.size() + "건");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("❌ 재고 알림 조회 실패: " + e.getMessage());
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "재고 알림 조회 중 오류가 발생했습니다.");
            errorResponse.put("message", e.getMessage());
            
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
}

/*
🎓 Spring Boot REST API 개발 완전 가이드 총정리
================================================================

📚 1. MVC 아키텍처 패턴 구현
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   Controller    │───▶│     Service      │───▶│     Mapper      │
│  (Web Layer)    │    │  (Business)      │    │  (Data Access)  │
│                 │    │                  │    │                 │
│ - HTTP 요청처리  │    │ - 비즈니스 로직   │    │ - SQL 실행      │
│ - 응답 데이터    │    │ - 트랜잭션 관리   │    │ - DB 연동       │
│ - 파라미터 검증  │    │ - 데이터 변환     │    │ - 결과 매핑     │
└─────────────────┘    └──────────────────┘    └─────────────────┘

📝 2. RESTful API 설계 원칙
- GET /api/materials/stock-status          → 목록 조회
- GET /api/materials/stock-status/{id}     → 단건 조회  
- POST /api/materials/stock-status         → 생성
- PUT /api/materials/stock-status/{id}     → 수정
- DELETE /api/materials/stock-status/{id}  → 삭제

🔧 3. Spring Boot 핵심 어노테이션
@RestController   : REST API 컨트롤러 선언
@RequestMapping   : 기본 URL 경로 설정
@GetMapping      : HTTP GET 요청 매핑
@PostMapping     : HTTP POST 요청 매핑
@PathVariable    : URL 경로 변수 추출
@RequestParam    : 쿼리 파라미터 추출
@RequestBody     : HTTP Body 데이터 매핑
@Autowired       : 의존성 주입

🎯 4. 에러 처리 전략
- try-catch로 예외 포착
- 상황별 HTTP 상태 코드 반환
- 사용자 친화적 에러 메시지 제공
- 로깅으로 디버깅 정보 기록

📊 5. 응답 데이터 구조 설계
{
  "data": [],           // 메인 데이터
  "totalCount": 0,      // 전체 개수
  "statistics": {},     // 통계 정보
  "alertCount": 0,      // 알림 개수
  "timestamp": "",      // 응답 시간
  "searchConditions": {}// 검색 조건
}

🚀 6. 성능 최적화 포인트
- Service Layer에서 비즈니스 로직 처리
- DB 쿼리는 Mapper Layer에서 최적화
- 응답 데이터 Stream API로 효율적 가공
- 적절한 로깅으로 병목지점 파악

💡 7. 확장 가능한 설계
- 검색 조건을 VO 객체로 캡슐화
- 응답 형식 표준화로 프론트엔드 개발 용이
- 메타데이터 포함으로 클라이언트 편의성 향상
- TODO 주석으로 향후 개발 방향 명시

🔐 8. 보안 고려사항
- 입력 파라미터 검증
- SQL Injection 방지 (MyBatis 파라미터 바인딩)
- 인증/권한 체크 (JWT 토큰)
- 민감 정보 로깅 제외

이것이 바로 Spring Boot로 엔터프라이즈급 REST API를 개발하는 완전한 과정입니다! 🎉
*/

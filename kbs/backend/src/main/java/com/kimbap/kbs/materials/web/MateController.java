package com.kimbap.kbs.materials.web;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
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
import jakarta.servlet.http.HttpSession;

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
        // 🎯 방법 1: JWT 토큰에서 cpCd 추출
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            try {
                String token = authHeader.substring(7);
                System.out.println("🔍 JWT 토큰 발견: " + token.substring(0, Math.min(20, token.length())) + "...");

                // JWT 토큰 파싱 시도 (JWT 유틸리티 클래스 필요)
                // 이 부분은 프로젝트의 JWT 구현에 따라 달라집니다
                /*
            if (jwtUtil != null) {
                String cpCd = jwtUtil.getCpCdFromToken(token);
                if (cpCd != null && !cpCd.isEmpty()) {
                    System.out.println("✅ JWT에서 cpCd 추출 성공: " + cpCd);
                    return cpCd;
                }
            }
                 */
            } catch (Exception e) {
                System.out.println("❌ JWT 토큰 파싱 실패: " + e.getMessage());
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

                // UserDetails 구현체에서 cpCd 추출
                if (auth.getPrincipal() instanceof UserDetails) {
                    UserDetails userDetails = (UserDetails) auth.getPrincipal();
                    // CustomUserDetails에 cpCd가 있다면
                    /*
                if (userDetails instanceof CustomUserDetails) {
                    String cpCd = ((CustomUserDetails) userDetails).getCpCd();
                    if (cpCd != null && !cpCd.isEmpty()) {
                        System.out.println("✅ UserDetails에서 cpCd 추출 성공: " + cpCd);
                        return cpCd;
                    }
                }
                     */
                }

                // Map 형태의 Details에서 추출
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

        // 🎯 마지막 대안: 기본값 반환
        System.out.println("⚠️ cpCd를 찾을 수 없어서 기본값 사용: CP-001");
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

    //
     // ✅ 날짜 문자열 변환 가능하게 설정
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd"); // ISO 8601
        sdf.setLenient(false);
        binder.registerCustomEditor(Date.class, new CustomDateEditor(sdf, true));
    }
    @GetMapping("/flow")
    public List<MaterialsVO> getMaterialFlowList(MaterialsVO search) {
        return mateService.getMaterialFlowList(search);
    }
}

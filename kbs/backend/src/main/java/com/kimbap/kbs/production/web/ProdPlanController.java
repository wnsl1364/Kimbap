package com.kimbap.kbs.production.web;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kimbap.kbs.production.mapper.ProdPlanMapper;
import com.kimbap.kbs.production.service.MrpPreviewVO;
import com.kimbap.kbs.production.service.ProdPlanDetailVO;
import com.kimbap.kbs.production.service.ProdPlanFullVO;
import com.kimbap.kbs.production.service.ProdPlanService;
import com.kimbap.kbs.production.service.ProdPlanVO;
import com.kimbap.kbs.production.service.ProdsVO;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/prod/prodPlan")
@RequiredArgsConstructor
public class ProdPlanController {
    private final ProdPlanService service;
    @Autowired
    private ProdPlanMapper mapper;

    @GetMapping("/list")
    public List<ProdPlanVO> list() {
        return service.getAllPlans();
    }
    // 생산계획 조건 검색
    @PostMapping("/search")
    public List<ProdPlanVO> searchPlans(@RequestBody ProdPlanVO condition) {
        return service.getPlansByCondition(condition);
    }
    // 생산계획코드별 생산계획상세 조회
    @GetMapping("/{produPlanCd}")
    public List<ProdPlanDetailVO> getDetailsByPlanCd(@PathVariable String produPlanCd) {
        return service.getDetailsByPlanCd(produPlanCd);
    }
    // 제품기준정보 ALL 검색
    @GetMapping("/productAll")
    public List<ProdsVO> getAllProducts() {
        return service.getAllProducts();
    }
    // 생산계획 및 상세 저장    
    @PostMapping("/planSave")
    public void saveProdPlan(@RequestBody ProdPlanFullVO fullVO) {
        service.saveProdPlan(fullVO);
    }
    // 생산계획과 관련 상세 삭제
    @DeleteMapping("/{produPlanCd}")
    public ResponseEntity<Void> deleteProdPlan(@PathVariable String produPlanCd) {
        service.deleteProdPlan(produPlanCd);
        return ResponseEntity.noContent().build();
    }
    // 개별 MRP 등록
    @PostMapping("/runMrp")
    public ResponseEntity<?> runMrp(@RequestParam String produPlanCd) {
        service.runMrpByProdPlan(produPlanCd);
        return ResponseEntity.ok("MRP 완료");
    }
    // 개별 발주서 생성
    @PostMapping("/createPurchaseOrder")
    public ResponseEntity<?> createPurchaseOrderFromMrp(@RequestParam String mrpCd) {
        try {
            service.createPurchaseOrderFromMrp(mrpCd);
            return ResponseEntity.ok("발주서 생성 완료");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("발주서 생성 실패: " + e.getMessage());
        }
    }

    // MRP 미리보기
    @PostMapping("/mrpPreview")
    public ResponseEntity<MrpPreviewVO> getMrpPreview(@RequestBody ProdPlanFullVO fullVO) {
        try {
            MrpPreviewVO preview = service.getMrpPreview(fullVO);
            return ResponseEntity.ok(preview);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    // 통합 저장 API - Service의 기존 메소드들을 직접 조합해서 사용
    @PostMapping("/planSaveWithMrp")
    public ResponseEntity<?> saveProdPlanWithMrp(@RequestBody Map<String, Object> request) {
        try {
            System.out.println("=== 통합 저장 API 호출 ===");
            System.out.println("Request data: " + request);
            
            // 사용자 정보 추출
            String empCd = extractEmpCd(request);
            String empName = extractEmpName(request);
            
            System.out.println("사용자 정보 - empCd: " + empCd + ", empName: " + empName);
            
            // ProdPlanFullVO 변환
            ProdPlanFullVO fullVO = convertToProdPlanFullVO(request);
            
            // Null 체크 및 디버깅
            if (fullVO == null) {
                throw new RuntimeException("ProdPlanFullVO 변환 실패");
            }
            if (fullVO.getPlan() == null) {
                throw new RuntimeException("ProdPlanVO가 null입니다. 데이터 변환을 확인하세요.");
            }
            
            System.out.println("변환된 fullVO - plan: " + fullVO.getPlan());
            System.out.println("생산계획 코드: " + fullVO.getPlan().getProduPlanCd());
            
            // 1. 기존 Service 메소드로 생산계획 저장
            service.saveProdPlan(fullVO);  // 기존 메소드 직접 호출
            String produPlanCd = fullVO.getPlan().getProduPlanCd();
            System.out.println("생산계획 저장 완료: " + produPlanCd);
            
            // 2. 기존 Service 메소드로 MRP + 발주서 생성
            String mrpCd = service.runMrpAndCreatePurchaseOrder(produPlanCd);  // 기존 메소드 직접 호출
            System.out.println("MRP + 발주서 생성 완료: " + mrpCd);
            
            // 3. 사용자 정보 업데이트 (Controller에서 직접 처리)
            if (empCd != null && !empCd.trim().isEmpty() && !"SYSTEM".equals(empCd)) {
                updateUserInfo(produPlanCd, mrpCd, empCd);
            }
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "생산계획, MRP, 발주서 생성 및 등록자 정보 업데이트 완료",
                "produPlanCd", produPlanCd,
                "mrpCd", mrpCd,
                "regi", empCd,
                "regiName", empName
            ));
            
        } catch (Exception e) {
            System.err.println("통합 저장 실패: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "처리 실패: " + e.getMessage()
            ));
        }
    }
    
    // 사용자 정보 업데이트 로직
    private void updateUserInfo(String produPlanCd, String mrpCd, String empCd) {
        try {
            System.out.println("사용자 정보 업데이트 시작 - 등록자: " + empCd);
            
            // 생산계획 등록자 업데이트
            mapper.updateProdPlanRegi(produPlanCd, empCd);
            System.out.println("생산계획 등록자 업데이트 완료: " + produPlanCd);
            
            // MRP 등록자 업데이트
            mapper.updateMrpRegi(mrpCd, empCd);
            System.out.println("MRP 등록자 업데이트 완료: " + mrpCd);
            
            // 최근 생성된 발주서 찾아서 업데이트
            String purcCd = mapper.getLatestPurchaseOrder();
            if (purcCd != null) {
                mapper.updatePurchaseOrderRegi(purcCd, empCd);
                System.out.println("발주서 등록자 업데이트 완료: " + purcCd);
            }
            
            System.out.println("모든 등록자 정보 업데이트 완료 - " + empCd);
            
        } catch (Exception e) {
            System.err.println("사용자 정보 업데이트 실패: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // 간단한 헬퍼 메소드들
    @SuppressWarnings("unchecked")
    private String extractEmpCd(Map<String, Object> request) {
        try {
            Map<String, Object> userInfo = (Map<String, Object>) request.get("userInfo");
            return userInfo != null ? (String) userInfo.get("empCd") : "SYSTEM";
        } catch (Exception e) {
            return "SYSTEM";
        }
    }
    
    @SuppressWarnings("unchecked")
    private String extractEmpName(Map<String, Object> request) {
        try {
            Map<String, Object> userInfo = (Map<String, Object>) request.get("userInfo");
            return userInfo != null ? (String) userInfo.get("empName") : "시스템";
        } catch (Exception e) {
            return "시스템";
        }
    }
    
    // 데이터 변환 로직 (Vue.js 데이터 구조에 맞춰 수정)
    @SuppressWarnings("unchecked")
    private ProdPlanFullVO convertToProdPlanFullVO(Map<String, Object> request) {
        System.out.println("=== ProdPlanFullVO 변환 시작 ===");
        
        ProdPlanFullVO fullVO = new ProdPlanFullVO();
        
        try {
            // plan 데이터 변환
            Object planObj = request.get("plan");
            System.out.println("Plan 객체: " + planObj);
            
            if (planObj instanceof Map) {
                Map<String, Object> planData = (Map<String, Object>) planObj;
                ProdPlanVO plan = new ProdPlanVO();
                
                // 기본 정보 설정
                if (planData.get("produPlanCd") != null) {
                    plan.setProduPlanCd((String) planData.get("produPlanCd"));
                }
                if (planData.get("planDt") != null) {
                    plan.setPlanDt(parseStringToDate((String) planData.get("planDt")));
                }
                if (planData.get("planStartDt") != null) {
                    plan.setPlanStartDt(parseStringToDate((String) planData.get("planStartDt")));
                }
                if (planData.get("planEndDt") != null) {
                    plan.setPlanEndDt(parseStringToDate((String) planData.get("planEndDt")));
                }
                if (planData.get("note") != null) {
                    plan.setNote((String) planData.get("note"));
                }
                if (planData.get("mname") != null) {
                    plan.setMname((String) planData.get("mname"));
                }
                
                // Vue.js의 factory 객체 처리
                Object factoryObj = planData.get("factory");
                System.out.println("Factory 객체: " + factoryObj);
                if (factoryObj instanceof Map) {
                    Map<String, Object> factoryData = (Map<String, Object>) factoryObj;
                    if (factoryData.get("fcode") != null) {
                        plan.setFcode((String) factoryData.get("fcode"));
                    }
                    if (factoryData.get("facVerCd") != null) {
                        plan.setFacVerCd((String) factoryData.get("facVerCd"));
                    }
                } else {
                    // factory가 직접 fcode, facVerCd로 전달되는 경우
                    if (planData.get("fcode") != null) {
                        plan.setFcode((String) planData.get("fcode"));
                    }
                    if (planData.get("facVerCd") != null) {
                        plan.setFacVerCd((String) planData.get("facVerCd"));
                    }
                }
                
                fullVO.setPlan(plan);
                System.out.println("Plan 변환 완료: " + plan);
                System.out.println("Plan - fcode: " + plan.getFcode() + ", facVerCd: " + plan.getFacVerCd());
            } else {
                System.err.println("Plan 객체가 Map이 아닙니다: " + planObj);
            }
            
            // planDetails 데이터 변환
            Object detailsObj = request.get("planDetails");
            System.out.println("PlanDetails 객체: " + detailsObj);
            
            if (detailsObj instanceof List) {
                List<Object> detailsList = (List<Object>) detailsObj;
                List<ProdPlanDetailVO> details = new ArrayList<>();
                
                for (Object detailObj : detailsList) {
                    if (detailObj instanceof Map) {
                        Map<String, Object> detailData = (Map<String, Object>) detailObj;
                        ProdPlanDetailVO detail = new ProdPlanDetailVO();
                        
                        if (detailData.get("ppdcode") != null) {
                            detail.setPpdcode((String) detailData.get("ppdcode"));
                        }
                        if (detailData.get("pcode") != null) {
                            detail.setPcode((String) detailData.get("pcode"));
                        }
                        if (detailData.get("prodVerCd") != null) {
                            detail.setProdVerCd((String) detailData.get("prodVerCd"));
                        }
                        if (detailData.get("planQty") != null) {
                            detail.setPlanQty(parseToInteger(detailData.get("planQty")));
                        }
                        if (detailData.get("unit") != null) {
                            detail.setUnit((String) detailData.get("unit"));
                        }
                        if (detailData.get("exProduDt") != null) {
                            detail.setExProduDt(parseStringToDate((String) detailData.get("exProduDt")));
                        }
                        if (detailData.get("seq") != null) {
                            detail.setSeq(parseToInteger(detailData.get("seq")));
                        }
                        
                        details.add(detail);
                        System.out.println("Detail 추가: pcode=" + detail.getPcode() + ", planQty=" + detail.getPlanQty());
                    }
                }
                
                fullVO.setPlanDetails(details);
                System.out.println("PlanDetails 변환 완료: " + details.size() + "개");
            } else {
                System.err.println("PlanDetails 객체가 List가 아닙니다: " + detailsObj);
            }
            
        } catch (Exception e) {
            System.err.println("데이터 변환 중 오류: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("데이터 변환 실패: " + e.getMessage());
        }
        
        System.out.println("=== ProdPlanFullVO 변환 완료 ===");
        System.out.println("Plan null 여부: " + (fullVO.getPlan() == null));
        System.out.println("Details size: " + (fullVO.getPlanDetails() != null ? fullVO.getPlanDetails().size() : "null"));
        
        return fullVO;
    }
    
    // 날짜 변환 헬퍼 메소드
    private Date parseStringToDate(String dateString) {
        try {
            if (dateString != null && !dateString.trim().isEmpty()) {
                // YYYY-MM-DD 형태의 문자열을 Date로 변환
                LocalDate localDate = LocalDate.parse(dateString);
                return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
            }
        } catch (Exception e) {
            System.err.println("날짜 변환 실패: " + dateString + ", 오류: " + e.getMessage());
        }
        return null;
    }
    
    // 정수 변환 헬퍼 메소드
    private Integer parseToInteger(Object value) {
        if (value == null) {
            return null;
        }
        try {
            if (value instanceof Integer) {
                return (Integer) value;
            } else if (value instanceof Number) {
                return ((Number) value).intValue();
            } else {
                return Integer.valueOf(value.toString());
            }
        } catch (NumberFormatException e) {
            System.err.println("숫자 변환 실패: " + value + ", 오류: " + e.getMessage());
            return null;
        }
    }
}

package com.kimbap.kbs.production.web;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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

    // 핵심: 생산계획 저장 + MRP + 발주서 생성 통합 API
    @PostMapping("/planSaveWithMrp")
    public ResponseEntity<?> saveProdPlanWithMrp(@RequestBody ProdPlanFullVO fullVO) {
        try {
            // 1. 생산계획 저장
            service.saveProdPlan(fullVO);
            
            // 2. MRP 실행 + 발주서 생성을 한 번에 처리
            String produPlanCd = fullVO.getPlan().getProduPlanCd();
            String mrpCd = service.runMrpAndCreatePurchaseOrder(produPlanCd);
            
            return ResponseEntity.ok(Map.of(
                "message", "생산계획, MRP, 발주서 생성 완료",
                "produPlanCd", produPlanCd,
                "mrpCd", mrpCd
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("처리 실패: " + e.getMessage());
        }
    }

}

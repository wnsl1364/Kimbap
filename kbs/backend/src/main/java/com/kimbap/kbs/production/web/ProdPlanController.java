package com.kimbap.kbs.production.web;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kimbap.kbs.production.service.ProdPlanFullVO;
import com.kimbap.kbs.production.service.ProdPlanService;
import com.kimbap.kbs.production.service.ProdPlanVO;

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

    @GetMapping("/{produPlanCd}")
    public ProdPlanFullVO get(@PathVariable String produPlanCd) {
        return service.getPlanWithDetails(produPlanCd);
    }

    @PostMapping
    public ResponseEntity<?> save(@RequestBody ProdPlanFullVO fullVO) {
        service.savePlanWithDetails(fullVO);
        return ResponseEntity.ok("저장 완료");
    }

    @DeleteMapping("/{produPlanCd}")
    public ResponseEntity<?> delete(@PathVariable String produPlanCd) {
        service.deletePlan(produPlanCd);
        return ResponseEntity.ok("삭제 완료");
    }
    // 생산계획 조건 검색
    @PostMapping("/search")
    public List<ProdPlanVO> searchPlans(@RequestBody ProdPlanVO condition) {
        return service.getPlansByCondition(condition);
    }
}

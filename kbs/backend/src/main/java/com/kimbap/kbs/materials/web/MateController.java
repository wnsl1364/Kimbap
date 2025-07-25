package com.kimbap.kbs.materials.web;

import java.util.List;

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
import com.kimbap.kbs.materials.service.SearchCriteria;

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

    // ========== 발주 관련 API (자재입고와 연관) ==========
    
    /**
     * 발주 목록 검색 및 조회 (자재입고 시 참조용)
     */
    @GetMapping("/purchaseOrders")
    public ResponseEntity<List<MaterialsVO>> getPurchaseOrders(
        @RequestParam(required = false) String purcCd,
        @RequestParam(required = false) String mateName,
        @RequestParam(required = false) String mcode,
        @RequestParam(required = false) String purcDStatus,    // 발주상세상태
        @RequestParam(required = false) String purcStatus,     // 발주상태 (추가!)
        @RequestParam(required = false) String cpCd,           // 회사코드 (추가!)
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
    
    

}
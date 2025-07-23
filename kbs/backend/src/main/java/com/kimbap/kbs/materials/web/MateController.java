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
import org.springframework.web.bind.annotation.RestController;

import com.kimbap.kbs.materials.service.MateInboVO;
import com.kimbap.kbs.materials.service.MateService;
import com.kimbap.kbs.materials.service.PurcOrdVO;

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
    public ResponseEntity<List<MateInboVO>> getMateInboList() {
        try {
            List<MateInboVO> list = mateService.getMateInboList();
            return ResponseEntity.ok(list);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 자재입고 단건 조회
     */
    @GetMapping("/inbound/{mateInboCd}")
    public ResponseEntity<MateInboVO> getMateInboById(@PathVariable String mateInboCd) {
        try {
            MateInboVO mateInbo = mateService.getMateInboById(mateInboCd);
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
    public ResponseEntity<String> insertMateInbo(@RequestBody MateInboVO mateInbo) {
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
    public ResponseEntity<String> updateMateInbo(@RequestBody MateInboVO mateInbo) {
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
     * 발주 목록 조회 (자재입고 시 참조용)
     */
    @GetMapping("/purchase-orders")
    public ResponseEntity<List<PurcOrdVO>> getPurcOrdList() {
        try {
            List<PurcOrdVO> list = mateService.getPurcOrdList();
            return ResponseEntity.ok(list);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    

}
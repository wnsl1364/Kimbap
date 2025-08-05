package com.kimbap.kbs.production.web;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kimbap.kbs.materials.service.MateLoadingVO;
import com.kimbap.kbs.production.service.ProdInboundVO;
import com.kimbap.kbs.production.service.ProdLoadingService;

@RestController
@RequestMapping("/api/prod/prodLoading")
@CrossOrigin(origins = "*")
public class ProdLoadingController {
  @Autowired
  private ProdLoadingService prodLoadingService;

  // 제품 적재 대기 목록 전체 조회
  @GetMapping("/waitList")
  public ResponseEntity<List<ProdInboundVO>> getAllMateLoadingWaitList() {
      try {
          List<ProdInboundVO> list = prodLoadingService.getAllProdLoadingWaitList();
          System.out.println("제품 적재 대기 목록 조회 완료: " + list.size() + "건");
          return ResponseEntity.ok(list);
      } catch (Exception e) {
          System.err.println("제품 적재 대기 목록 조회 실패: " + e.getMessage());
          e.printStackTrace();
          return ResponseEntity.internalServerError().build();
      }
  }
  // 구역 적재 가능 여부 검증
  @GetMapping("/validate-area")
  public ResponseEntity<Map<String, Object>> validateAreaAllocation(
    @RequestParam String wareAreaCd,
    @RequestParam String pcode,
    @RequestParam Integer allocateQty) {
    try {
      System.out.println("구역 적재 검증 요청: " + wareAreaCd + " - " + pcode + " - " + allocateQty);
      
      Map<String, Object> validationResult = prodLoadingService.validateAreaAllocation(wareAreaCd, pcode, allocateQty);
      
      System.out.println("구역 적재 검증 완료: " + validationResult.get("message"));
      return ResponseEntity.ok(validationResult);
        
    } catch (Exception e) {
      System.err.println("구역 적재 검증 실패: " + e.getMessage());
      e.printStackTrace();
      
      Map<String, Object> errorResponse = new HashMap<>();
      errorResponse.put("isValid", false);
      errorResponse.put("message", "구역 적재 검증 중 오류가 발생했습니다: " + e.getMessage());
      
      return ResponseEntity.internalServerError().body(errorResponse);
    }
  }

  // 동일한 자재가 적재된 다른 구역들 조회 (분할 적재용)
  @GetMapping("/same-product-areas")
  public ResponseEntity<List<ProdInboundVO>> getSameProductAreas(
          @RequestParam String pcode,
          @RequestParam String fcode,
          @RequestParam(required = false) String excludeAreaCd) {
      try {
          System.out.println("동일 제품 적재 구역 조회 요청: " + pcode + " - " + fcode);
          
          if (excludeAreaCd == null) excludeAreaCd = "";
          
          List<ProdInboundVO> sameProductAreas = prodLoadingService.getSameProductAreas(pcode, fcode, excludeAreaCd);
          
          System.out.println("동일 제품 적재 구역 조회 완료: " + pcode + " - " + sameProductAreas.size() + "개 구역");
          return ResponseEntity.ok(sameProductAreas);
          
      } catch (Exception e) {
          System.err.println("동일 제품 적재 구역 조회 실패: " + pcode + " - " + e.getMessage());
          e.printStackTrace();
          return ResponseEntity.internalServerError().build();
      }
  }
}

package com.kimbap.kbs.production.web;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
  // 단건 자재 적재 처리
  @PostMapping("/processSingle")
  public ResponseEntity<Map<String, Object>> processProdLoading(@RequestBody ProdInboundVO prodLoading) {
      try {
          System.out.println("단건 제품 적재 처리 요청: " + prodLoading.getProdInboCd());
          
          String result = prodLoadingService.processProdLoading(prodLoading);
          
          Map<String, Object> response = new HashMap<>();
          response.put("success", true);
          response.put("message", result);
          response.put("mateInboCd", prodLoading.getProdInboCd());
          
          System.out.println("단건 제품 적재 처리 완료: " + prodLoading.getProdInboCd());
          return ResponseEntity.ok(response);
          
      } catch (Exception e) {
          System.err.println("단건 제품 적재 처리 실패: " + e.getMessage());
          e.printStackTrace();
          
          Map<String, Object> errorResponse = new HashMap<>();
          errorResponse.put("success", false);
          errorResponse.put("message", "제품 적재 처리 중 오류가 발생했습니다: " + e.getMessage());
          
          return ResponseEntity.internalServerError().body(errorResponse);
      }
  }

  // 다중 자재 적재 처리 (선택된 여러 자재 한번에 처리)
  @PostMapping("/processBatch")
  public ResponseEntity<Map<String, Object>> processProdLoadingBatch(@RequestBody List<ProdInboundVO> prodLoadingList) {
      try {
          System.out.println("=== 다중 제품 적재 처리 요청 ===");
          System.out.println("요청 건수: " + prodLoadingList.size());
          
          // 각 항목의 상세 정보 로그
          for (int i = 0; i < prodLoadingList.size(); i++) {
              ProdInboundVO item = prodLoadingList.get(i);
              System.out.println(String.format("[%d] prodInboCd: %s, pcode: %s, qty: %s, wareAreaCd: %s, unit: %s", 
                  i+1, item.getProdInboCd(), item.getPcode(), item.getQty(), item.getWareAreaCd(), item.getUnit()));
          }
          
          if (prodLoadingList.isEmpty()) {
              Map<String, Object> response = new HashMap<>();
              response.put("success", false);
              response.put("message", "처리할 제품을 선택해주세요.");
              
              return ResponseEntity.badRequest().body(response);
          }
          
          String result = prodLoadingService.processProdLoadingBatch(prodLoadingList);
          
          Map<String, Object> response = new HashMap<>();
          response.put("success", true);
          response.put("message", result);
          response.put("processedCount", prodLoadingList.size());
          
          System.out.println("다중 제품 적재 처리 완료: " + prodLoadingList.size() + "건");
          return ResponseEntity.ok(response);
          
      } catch (Exception e) {
          System.err.println("다중 제품 적재 처리 실패: " + e.getMessage());
          e.printStackTrace();
          
          Map<String, Object> errorResponse = new HashMap<>();
          errorResponse.put("success", false);
          errorResponse.put("message", "다중 제품 적재 처리 중 오류가 발생했습니다: " + e.getMessage());
          
          return ResponseEntity.internalServerError().body(errorResponse);
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
  // 특정 창고의 구역 정보 조회 (층별, 현재 적재 상황 포함)

@GetMapping("/warehouse-areas")
public ResponseEntity<List<Map<String, Object>>> getWarehouseAreasWithStock(
        @RequestParam String wcode, 
        @RequestParam Integer floor) {
    try {
        System.out.println("창고 구역별 적재 현황 조회 요청: " + wcode + " " + floor + "층");
        
        List<Map<String, Object>> areaList = prodLoadingService.getWarehouseAreasWithStock(wcode, floor);
        
        System.out.println("창고 구역별 적재 현황 조회 완료: " + wcode + " " + floor + "층 - " + areaList.size() + "개 구역");
        return ResponseEntity.ok(areaList);
        
    } catch (Exception e) {
        System.err.println("창고 구역별 적재 현황 조회 실패: " + wcode + " " + floor + "층 - " + e.getMessage());
        e.printStackTrace();
        return ResponseEntity.internalServerError().build();
    }
}
    
}

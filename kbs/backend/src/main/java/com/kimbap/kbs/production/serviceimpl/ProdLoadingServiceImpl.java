package com.kimbap.kbs.production.serviceimpl;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kimbap.kbs.production.mapper.ProdLoadingMapper;
import com.kimbap.kbs.production.service.ProdInboundVO;
import com.kimbap.kbs.production.service.ProdLoadingService;

@Service
@Transactional
public class ProdLoadingServiceImpl implements ProdLoadingService {

  @Autowired
  private ProdLoadingMapper prodLoadingMapper;

  // 제품 적재 대기 목록 전체 조회
  @Override
  public List<ProdInboundVO> getAllProdLoadingWaitList() {
    List<ProdInboundVO> list = prodLoadingMapper.getAllProdLoadingWaitList();
    return list;
  }
  // 구역 적재 가능 여부 검증
  @Override
    public Map<String, Object> validateAreaAllocation(String wareAreaCd, String pcode, Integer allocateQty) {
      Map<String, Object> result = new HashMap<>();
      
      try {
        // 현재 적재량 조회
        Integer currentVolume = prodLoadingMapper.getCurrentVolumeByArea(wareAreaCd);
        if (currentVolume == null) currentVolume = 0;
        
        // 구역 정보 조회 (용량 확인용)
        ProdInboundVO areaStock = prodLoadingMapper.getWarehouseAreaStock(wareAreaCd);
        
        if (areaStock == null) {
          result.put("isValid", false);
          result.put("message", "해당 구역 정보를 찾을 수 없습니다.");
          return result;
        }
        
        // 현재 적재된 자재 확인
        String currentProduct = prodLoadingMapper.getCurrentProductByArea(wareAreaCd);
        
        // 다른 자재가 적재되어 있는지 확인
        if (currentProduct != null && !currentProduct.equals(pcode)) {
          result.put("isValid", false);
          result.put("message", "해당 구역에는 다른 제품(" + currentProduct + ")이 적재되어 있습니다.");
          result.put("currentProduct", currentProduct);
          return result;
        }
        
        // 용량 확인 - 실제 구역 용량 조회
        int maxVolume = (areaStock.getVol() != null) ? areaStock.getVol().intValue() : 100; // 기본값 100
        int availableVolume = maxVolume - currentVolume;
        
        if (allocateQty > availableVolume) {
          result.put("isValid", false);
          result.put("message", String.format("구역 용량이 부족합니다. 잔여용량: %d, 요청수량: %d", 
                                              availableVolume, allocateQty));
          result.put("availableVolume", availableVolume);
          result.put("currentVolume", currentVolume);
          result.put("maxVolume", maxVolume);
          return result;
        }
        
        // 검증 통과
        result.put("isValid", true);
        result.put("message", "적재 가능합니다.");
        result.put("availableVolume", availableVolume);
        result.put("currentVolume", currentVolume);
        result.put("maxVolume", maxVolume);
        result.put("currentProduct", currentProduct);
        
        return result;
        
    } catch (Exception e) {
      System.err.println("구역 적재 검증 실패: " + wareAreaCd + " - " + e.getMessage());
      result.put("isValid", false);
      result.put("message", "구역 적재 검증 중 오류가 발생했습니다: " + e.getMessage());
      return result;
    }
  }

  @Override
  public List<ProdInboundVO> getSameProductAreas(String pcode, String fcode, String excludeAreaCd) {
    try {
      List<ProdInboundVO> sameProductAreas = prodLoadingMapper.getSameProductAreas(pcode, fcode, excludeAreaCd);
      System.out.println("동일 제품 적재 구역 조회 완료: " + pcode + " - " + sameProductAreas.size() + "개 구역");
      return sameProductAreas;
    } catch (Exception e) {
      System.err.println("동일 제품 적재 구역 조회 실패: " + pcode + " - " + e.getMessage());
      throw new RuntimeException("동일 제품 적재 구역 조회 실패: " + e.getMessage(), e);
    }
  }

  
  @Override
  public String processProdLoading(ProdInboundVO prodLoading) {
    System.out.println("=== 단건 제품 처리 시작 ===");
    System.out.println("입력데이터: " + prodLoading.toString());
    
    // product 테이블에서 자재 정보 조회
    ProdInboundVO productInfo = null;
    try {
        productInfo = prodLoadingMapper.getProductInfo(prodLoading.getPcode());
    } catch (Exception e) {
        System.err.println("제품 정보 조회 실패: " + prodLoading.getPcode() + " - " + e.getMessage());
        e.printStackTrace();
    }
    
    if (productInfo == null) {
        System.err.println("제품 정보를 찾을 수 없습니다: " + prodLoading.getPcode());
        // 기본값으로 처리 계속 진행
        prodLoading.setItemType("h3"); // 기본값: 제품 (품목유형 코드)
        // unit은 기존 값 유지
    } else {  
        // unit을 material 테이블의 unit(공통코드)으로 설정
        prodLoading.setUnit(productInfo.getUnit());
    }
    
    // 현재 시간 설정
    prodLoading.setInboDt(Timestamp.valueOf(LocalDateTime.now()));
    
    // 창고재고목록코드 생성
    String wslcode = generateWareStockCode();
    prodLoading.setWslcode(wslcode);
    
    // 🔥 등록자 설정 - 프론트엔드에서 전달된 empCd 사용
    if (prodLoading.getRegi() == null || prodLoading.getRegi().trim().isEmpty()) {
        prodLoading.setRegi("system"); // 기본값
    }
    
    System.out.println("처리 전 최종 데이터:");
    System.out.println("  wslcode: " + prodLoading.getWslcode());
    System.out.println("  wareAreaCd: " + prodLoading.getWareAreaCd());
    System.out.println("  mateInboCd: " + prodLoading.getProdInboCd());
    System.out.println("  qty: " + prodLoading.getQty());
    System.out.println("  unit: " + prodLoading.getUnit() + " (product 테이블에서 조회)");
    System.out.println("  regi: " + prodLoading.getRegi());
    System.out.println("  itemType: " + prodLoading.getItemType() + " (product 테이블에서 조회)");
    System.out.println("  inboDt: " + prodLoading.getInboDt());
    
    // ware_stock 테이블에 적재 정보 저장
    try {
        prodLoadingMapper.insertWareStock(prodLoading);
        System.out.println("=== ware_stock INSERT 성공 ===");
    } catch (Exception e) {
        System.err.println("=== ware_stock INSERT 실패 ===");
        System.err.println("에러: " + e.getMessage());
        e.printStackTrace();
        throw e;
    }
    
    return "자재 적재 처리가 완료되었습니다.";
  }
    
    @Override
    public String processProdLoadingBatch(List<ProdInboundVO> prodLoadingList) {
        System.out.println("=== 다중 적재 처리 시작 ===");
        System.out.println("처리할 항목 수: " + prodLoadingList.size());
        
        int successCount = 0;
        int failCount = 0;
        
        for (int i = 0; i < prodLoadingList.size(); i++) {
            ProdInboundVO prodLoading = prodLoadingList.get(i);
            System.out.println(String.format("=== [%d/%d] 처리 중 ===", i+1, prodLoadingList.size()));
            
            try {
                // 🔥 material 테이블에서 자재 정보 조회
                ProdInboundVO productInfo = null;
                try {
                    productInfo = prodLoadingMapper.getProductInfo(prodLoading.getPcode());
                } catch (Exception e) {
                    System.err.println("제품 정보 조회 실패: " + prodLoading.getPcode() + " - " + e.getMessage());
                }
                
                // 현재 시간 설정
                prodLoading.setInboDt(Timestamp.valueOf(LocalDateTime.now()));
                
                // 창고재고목록코드 생성
                String wslcode = generateWareStockCode();
                prodLoading.setWslcode(wslcode);
                
                // 등록자 설정 - 프론트엔드에서 전달된 empCd 사용
                if (prodLoading.getRegi() == null || prodLoading.getRegi().trim().isEmpty()) {
                    prodLoading.setRegi("system"); // 기본값
                }
                
                if (productInfo == null) {
                    System.err.println("제품 정보를 찾을 수 없습니다: " + prodLoading.getPcode());
                    // 기본값으로 처리 계속 진행
                    prodLoading.setItemType("h3"); // 기본값: 원자재 (품목유형 코드)
                    // unit은 기존 값 유지
                } else {
                    // 🔥 unit을 material 테이블의 unit(공통코드)으로 설정
                    prodLoading.setUnit(prodLoading.getUnit());
                }
                
                System.out.println("처리 데이터:");
                System.out.println("  prodInboCd: " + prodLoading.getProdInboCd());
                System.out.println("  pcode: " + prodLoading.getPcode());
                System.out.println("  wareAreaCd: " + prodLoading.getWareAreaCd());
                System.out.println("  qty: " + prodLoading.getQty());
                System.out.println("  unit: " + prodLoading.getUnit() + " (product 테이블에서 조회)");
                System.out.println("  regi: " + prodLoading.getRegi());
                System.out.println("  itemType: " + prodLoading.getItemType() + " (product 테이블에서 조회)");
                System.out.println("  wslcode: " + wslcode);
                
                // ware_stock 테이블에 적재 정보 저장
                prodLoadingMapper.insertWareStock(prodLoading);
                
                successCount++;
                System.out.println("적재 처리 성공: " + prodLoading.getProdInboCd() + " -> " + wslcode);
                
            } catch (Exception e) {
                failCount++;
                System.err.println("적재 처리 실패: " + prodLoading.getProdInboCd() + " - " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        String result = String.format("다중 자재 적재 처리 완료 - 성공: %d건, 실패: %d건", 
                                     successCount, failCount);
        
        System.out.println("=== 다중 적재 처리 완료 ===");
        System.out.println(result);
        
        return result;
    }
  @Override
  public String generateWareStockCode() {
    try {
      // 현재 날짜 (yyMMdd 형식)
      String datePattern = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMdd"));
      
      // 해당 날짜의 마지막 순번 조회
      int lastSequence = prodLoadingMapper.getLastWareStockSequence(datePattern);
      
      // 다음 순번 계산 (1부터 시작)
      int nextSequence = lastSequence + 1;
      
      // WStock-yyMMdd-순번 형식으로 생성
      String wslCode = String.format("WStock-%s-%03d", datePattern, nextSequence);
      
      System.out.println("창고재고목록코드 생성: " + wslCode);
      return wslCode;
        
    } catch (Exception e) {
      System.err.println("창고재고목록코드 생성 실패: " + e.getMessage());
      
      // 실패 시 임시 코드 생성
      String datePattern = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMdd"));
      long timestamp = System.currentTimeMillis() % 1000;
      String fallbackCode = String.format("WStock-%s-%03d", datePattern, (int) timestamp);
      
      System.out.println("임시 창고재고목록코드 생성: " + fallbackCode);
      return fallbackCode;
    }
  }
}

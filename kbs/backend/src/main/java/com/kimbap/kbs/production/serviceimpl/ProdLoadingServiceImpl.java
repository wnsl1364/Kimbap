package com.kimbap.kbs.production.serviceimpl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kimbap.kbs.materials.service.MateLoadingVO;
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
}

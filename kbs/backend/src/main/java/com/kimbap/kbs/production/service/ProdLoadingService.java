package com.kimbap.kbs.production.service;

import java.util.List;
import java.util.Map;

public interface ProdLoadingService {
  
  // 제품 적재 대기 목록 전체 조회
  List<ProdInboundVO> getAllProdLoadingWaitList();

  // 구역 적재 가능 여부 검증
  Map<String, Object> validateAreaAllocation(String wareAreaCd, String pcode, Integer allocateQty);

  // 동일한 제품이 적재된 다른 구역들 조회 (분할 적재용)
  List<ProdInboundVO> getSameProductAreas(String pcode, String fcode, String excludeAreaCd);
  // 제품 단건 적재 처리
  String processProdLoading(ProdInboundVO prodLoading);
  // 제품 다중 적재 처리
  String processProdLoadingBatch(List<ProdInboundVO> prodLoadingList);  
  // 창고재고목록코드 생성
  String generateWareStockCode();
  // 특정 창고의 구역 정보 조회 (층별, 현재 적재 상황 포함)
  List<Map<String, Object>> getWarehouseAreasWithStock(String wcode, Integer floor);
}

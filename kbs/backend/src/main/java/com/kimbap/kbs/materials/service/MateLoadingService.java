package com.kimbap.kbs.materials.service;

import java.util.List;
import java.util.Map;

public interface MateLoadingService {
  // 자재 적재 대기 목록 전체 조회
  List<MateLoadingVO> getAllMateLoadingWaitList();
  // 단건조회
  MateLoadingVO getMateLoadingByInboCd(String mateInboCd);
  // 적재 처리
  String processMateLoading(MateLoadingVO mateLoading);
  // 자재 적재 처리
  String processMateLoadingBatch(List<MateLoadingVO> mateLoadingList);
  // 활성화된 공장 목록 조회 (드롭다운)
  List<MateLoadingVO> getActiveFactoryList();
  // 창고 구역별 wslcode 조회
  String getWslCodeByArea(String wareAreaCd);
  

  // 특정 공장의 창고 목록 조회 (창고 유형별)
  List<MateLoadingVO> getWarehousesByFactory(String fcode);
  

  // 특정 창고의 구역 정보 조회 (층별, 현재 적재 상황 포함)
  List<Map<String, Object>> getWarehouseAreasWithStock(String wcode, Integer floor);
  

  //  창고구역코드 조회
  String getWareAreaCode(String wcode, String areaRow, Integer areaCol, Integer areaFloor);
  

  // 구역 적재 가능 여부 검증
  Map<String, Object> validateAreaAllocation(String wareAreaCd, String mcode, Integer allocateQty);
  

  // 동일한 자재가 적재된 다른 구역들 조회 (분할 적재용)
  List<MateLoadingVO> getSameMaterialAreas(String mcode, String fcode, String excludeAreaCd);
  

  // 창고재고목록코드 생성
  String generateWareStockCode();
}

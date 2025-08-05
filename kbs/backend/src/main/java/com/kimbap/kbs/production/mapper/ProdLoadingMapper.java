package com.kimbap.kbs.production.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.kimbap.kbs.production.service.ProdInboundVO;

public interface ProdLoadingMapper {
  // 제품 적재 대기 목록 전체 조회()
  List<ProdInboundVO> getAllProdLoadingWaitList();
  //구역별 현재 적재량 합계 조회
	Integer getCurrentVolumeByArea(@Param("wareAreaCd") String wareAreaCd);
  //구역에 적재된 제품코드 조회(동일제품 체크용)
  String getCurrentProductByArea(@Param("wareAreaCd") String wareAreaCd);
  //구역별 현재 적재 상황 조회
  ProdInboundVO getWarehouseAreaStock(@Param("wareAreaCd") String wareAreaCd);
  //동일한 자재가 적재된 다른 구역들 조회(분할적재용)
  List<ProdInboundVO> getSameProductAreas(@Param("pcode") String pcode,
                                          @Param("fcode") String fcode,
                                          @Param("excludeAreaCd") String excludeAreaCd);
}

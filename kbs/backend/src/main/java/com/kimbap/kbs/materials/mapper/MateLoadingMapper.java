package com.kimbap.kbs.materials.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.kimbap.kbs.materials.service.MateLoadingVO;

public interface MateLoadingMapper {
 
  // 자재 적재 대기 목록 전체 조회()
  List<MateLoadingVO> getAllMateLoadingWaitList();
  // 단건조회
  MateLoadingVO getMateLoadingByInboCd(@Param("mateInboCd") String mateInboCd);
  // 적재 처리
  void insertWareStock(MateLoadingVO mateLoading);
  // 활성화된 공장 목록 조회 (드롭다운)
  List<MateLoadingVO> getActiveFactoryList();
  // 창고 구역별 wslcode 조회
  String getWslCodeByArea(@Param("wareAreaCd") String wareAreaCd);
}

package com.kimbap.kbs.materials.service;

import java.util.List;

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
}

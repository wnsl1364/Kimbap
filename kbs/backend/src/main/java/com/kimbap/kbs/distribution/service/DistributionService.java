package com.kimbap.kbs.distribution.service;

import java.util.List;

public interface DistributionService {

  // 입출고 조회
  List<DistributionVO> getInOutCheck(DistributionVO filter);

  // 출고 지시서 조회
  List<RelOrderAndResultVO> getRelOrdList(RelOrderAndResultVO filter);

  // 출고지시서 등록 모달관련
  List<RelOrdModalVO> getRelOrdModal(RelOrdModalVO vo);

  // 모달 선택 후 주문 상세 출력
  List<RelOrdModalVO> getRelOrdSelect(String ordCd);

  // 창고 목록 조회
  List<WarehouseVO> getWarehouseListByOrdCd(String ordCd);

  // 출고지시 전체 저장
  void saveReleaseOrder(ReleaseMasterOrdVO masterVO, List<ReleaseOrdVO> detailList);

  // 출고지시서 불러오기 
  List<RelOrderAndResultVO> getRelOrdListWaiting();
  // 출고지시서 단건조회 
  List<RelDetailVO> getRelDetails(String relMasCd);
  // lot분배
  List<LotStockVO> getLotsByPcode(String pcode);

  String insertRelease(ReleaseRequestVO vo);
}

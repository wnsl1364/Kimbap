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
}

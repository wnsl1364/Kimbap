package com.kimbap.kbs.distribution.service;

import java.util.List;

public interface DistributionService {

  // 입출고 조회
  List<DistributionVO> getInOutCheck(DistributionVO filter);

  // 출고 지시서 조회
  List<RelOrderAndResultVO> getRelOrdList(RelOrderAndResultVO filter);

}

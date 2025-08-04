package com.kimbap.kbs.distribution.service;

import java.util.List;

public interface DistributionService {
  // 입고

  // 출고
  List<DistributionVO> getInOutCheck();
}

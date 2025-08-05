package com.kimbap.kbs.distribution.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.kimbap.kbs.distribution.service.DistributionVO;
import com.kimbap.kbs.distribution.service.RelOrderAndResultVO;

@Mapper
public interface DistributionMapper {
  // 입출고조회
  List<DistributionVO> getInOutCheck(DistributionVO filter);

  // 출고 지시서 조회
  List<RelOrderAndResultVO> getRelOrdList();
}

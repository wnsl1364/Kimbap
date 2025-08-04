package com.kimbap.kbs.distribution.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.kimbap.kbs.distribution.service.DistributionVO;

@Mapper
public interface DistributionMapper {
  // 입출고조회
  public List<DistributionVO> getInOutCheck();
}

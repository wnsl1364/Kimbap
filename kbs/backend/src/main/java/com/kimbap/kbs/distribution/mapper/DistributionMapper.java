package com.kimbap.kbs.distribution.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.kimbap.kbs.distribution.service.DistributionVO;
import com.kimbap.kbs.distribution.service.RelOrdModalVO;
import com.kimbap.kbs.distribution.service.RelOrderAndResultVO;
import com.kimbap.kbs.distribution.service.WarehouseVO;

@Mapper
public interface DistributionMapper {
  // 입출고조회
  List<DistributionVO> getInOutCheck(DistributionVO filter);

  // 출고 지시서 조회
  List<RelOrderAndResultVO> getRelOrdList(RelOrderAndResultVO filter);

  // 출고 지시서 등록 모달관련
  List<RelOrdModalVO> getRelOrdModal(RelOrdModalVO vo);

  // 모달 선택 후 주문 상세 출력
  List<RelOrdModalVO> getRelOrdSelect(String ordCd);

  // 창고 목록 조회
  List<WarehouseVO> getWarehouseListByOrdCd(String ordCd);
}

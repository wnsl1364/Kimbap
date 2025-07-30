package com.kimbap.kbs.production.serviceimpl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.kimbap.kbs.production.mapper.ProdRequestMapper;
import com.kimbap.kbs.production.service.ProdPlanDetailVO;
import com.kimbap.kbs.production.service.ProdRequestDetailVO;
import com.kimbap.kbs.production.service.ProdRequestService;
import com.kimbap.kbs.production.service.ProdRequestVO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProdRequestServiceImpl implements ProdRequestService {
  
  @Autowired  
  private final ProdRequestMapper mapper;

  // 생산요청 조건 검색
  @Override
    public List<ProdRequestVO> getRequestByCondition(ProdRequestVO condition) {
    return mapper.selectProdRequestByCondition(condition);
  }
  // 생산요청코드별 생산요청상세 조회
  @Override
  public List<ProdRequestDetailVO> getDetailsByReqCd(String produReqCd) {
      return mapper.selectDetailsByProduReqCd(produReqCd);
  }
}
